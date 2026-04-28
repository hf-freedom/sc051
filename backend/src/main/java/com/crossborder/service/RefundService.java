package com.crossborder.service;

import com.crossborder.model.*;
import com.crossborder.store.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RefundService {

    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
    private static final AtomicLong REFUND_COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private MemoryStore memoryStore;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    public Refund applyRefund(String orderId, String reason) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() == Order.OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("订单尚未支付，无法申请退款");
        }

        if (!canRefund(order.getStatus())) {
            throw new RuntimeException("当前订单状态不支持退款");
        }

        Refund refund = new Refund();
        refund.setId(UUID.randomUUID().toString());
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(orderId);
        refund.setUserId(order.getUserId());
        refund.setType(Refund.RefundType.FULL_REFUND);
        refund.setStatus(Refund.RefundStatus.PENDING);
        refund.setOriginalOrderStatus(order.getStatus());
        refund.setTotalAmount(order.getActualAmount());
        refund.setTaxAmount(order.getTaxAmount());
        refund.setReason(reason);
        refund.setApplyTime(LocalDateTime.now());

        calculateRefundAmount(refund, order);

        memoryStore.getRefunds().put(refund.getId(), refund);

        order.setStatus(Order.OrderStatus.REFUNDING);
        order.setUpdateTime(LocalDateTime.now());

        logger.info("退款申请创建成功, 订单号: {}, 退款单号: {}", order.getOrderNo(), refund.getRefundNo());
        return refund;
    }

    public Refund approveRefund(String refundId) {
        Refund refund = memoryStore.getRefunds().get(refundId);
        if (refund == null) {
            throw new RuntimeException("退款单不存在");
        }

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("退款单状态不正确");
        }

        refund.setStatus(Refund.RefundStatus.APPROVED);
        refund.setApproveTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        logger.info("退款申请已批准, 退款单号: {}", refund.getRefundNo());
        return processRefund(refundId);
    }

    public Refund processRefund(String refundId) {
        Refund refund = memoryStore.getRefunds().get(refundId);
        if (refund == null) {
            throw new RuntimeException("退款单不存在");
        }

        if (refund.getStatus() != Refund.RefundStatus.APPROVED) {
            throw new RuntimeException("退款单未批准，无法处理");
        }

        refund.setStatus(Refund.RefundStatus.PROCESSING);
        refund.setUpdateTime(LocalDateTime.now());

        Order order = orderService.getOrderById(refund.getOrderId());
        if (order != null) {
            for (OrderItem item : order.getItems()) {
                Product product = memoryStore.getProducts().get(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                }
            }
        }

        if (!Boolean.TRUE.equals(refund.getQuotaRestored())) {
            userService.restoreQuota(refund.getUserId(), refund.getActualRefundAmount());
            refund.setQuotaRestored(true);
        }

        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund.setCompleteTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        if (order != null) {
            order.setStatus(Order.OrderStatus.REFUNDED);
            order.setUpdateTime(LocalDateTime.now());
        }

        logger.info("退款处理完成, 退款单号: {}, 退款金额: {}", 
                refund.getRefundNo(), refund.getActualRefundAmount());
        return refund;
    }

    public Refund rejectRefund(String refundId, String reason) {
        Refund refund = memoryStore.getRefunds().get(refundId);
        if (refund == null) {
            throw new RuntimeException("退款单不存在");
        }

        if (refund.getStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("退款单状态不正确");
        }

        refund.setStatus(Refund.RefundStatus.REJECTED);
        refund.setUpdateTime(LocalDateTime.now());

        Order order = orderService.getOrderById(refund.getOrderId());
        if (order != null) {
            if (refund.getOriginalOrderStatus() != null) {
                order.setStatus(refund.getOriginalOrderStatus());
            } else {
                order.setStatus(Order.OrderStatus.PENDING_CUSTOMS);
            }
            order.setUpdateTime(LocalDateTime.now());
        }

        logger.info("退款申请已拒绝, 退款单号: {}, 原因: {}", refund.getRefundNo(), reason);
        return refund;
    }

    private void calculateRefundAmount(Refund refund, Order order) {
        BigDecimal actualRefundAmount;
        BigDecimal deductAmount = BigDecimal.ZERO;
        String deductReason = null;

        if (order.getStatus() == Order.OrderStatus.PENDING_CUSTOMS) {
            actualRefundAmount = order.getActualAmount();
            deductReason = "未报关，全额退款";
        } else if (order.getStatus() == Order.OrderStatus.CUSTOMS_PROCESSING) {
            actualRefundAmount = order.getActualAmount();
            deductReason = "报关处理中，全额退款";
        } else if (order.getStatus() == Order.OrderStatus.CUSTOMS_SUCCESS ||
                   order.getStatus() == Order.OrderStatus.PENDING_SHIPMENT) {
            BigDecimal taxAmount = order.getTaxAmount();
            BigDecimal serviceFee = order.getActualAmount()
                    .multiply(SERVICE_FEE_RATE)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            deductAmount = taxAmount.add(serviceFee);
            actualRefundAmount = order.getActualAmount().subtract(deductAmount).max(BigDecimal.ZERO);
            deductReason = "报关成功后取消，扣除税费和服务费";
        } else if (order.getStatus() == Order.OrderStatus.SHIPPED) {
            BigDecimal taxAmount = order.getTaxAmount();
            BigDecimal serviceFee = order.getActualAmount()
                    .multiply(SERVICE_FEE_RATE)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            deductAmount = taxAmount.add(serviceFee);
            actualRefundAmount = order.getActualAmount().subtract(deductAmount).max(BigDecimal.ZERO);
            deductReason = "已发货，扣除税费和服务费";
        } else {
            actualRefundAmount = order.getActualAmount();
            deductReason = "全额退款";
        }

        refund.setDeductAmount(deductAmount);
        refund.setActualRefundAmount(actualRefundAmount);
        refund.setDeductReason(deductReason);
    }

    private boolean canRefund(Order.OrderStatus status) {
        switch (status) {
            case PENDING_CUSTOMS:
            case CUSTOMS_PROCESSING:
            case CUSTOMS_SUCCESS:
            case PENDING_SHIPMENT:
            case SHIPPED:
                return true;
            default:
                return false;
        }
    }

    public Refund getRefundById(String refundId) {
        return memoryStore.getRefunds().get(refundId);
    }

    public List<Refund> getRefundsByOrderId(String orderId) {
        List<Refund> result = new ArrayList<>();
        for (Refund refund : memoryStore.getAllRefunds()) {
            if (refund.getOrderId().equals(orderId)) {
                result.add(refund);
            }
        }
        return result;
    }

    public List<Refund> getAllRefunds() {
        return memoryStore.getAllRefunds();
    }

    private String generateRefundNo() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = REFUND_COUNTER.incrementAndGet();
        return "RF" + datePart + String.format("%08d", sequence);
    }
}
