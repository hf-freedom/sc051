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
public class LogisticsService {

    private static final Logger logger = LoggerFactory.getLogger(LogisticsService.class);
    private static final AtomicLong LOGISTICS_COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private MemoryStore memoryStore;

    @Autowired
    private OrderService orderService;

    public Logistics createShipment(String orderId, String fromAddress, String toAddress, 
                                     String receiverName, String receiverPhone) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() == Order.OrderStatus.REFUNDING || 
            order.getStatus() == Order.OrderStatus.REFUNDED ||
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已退款或已取消，无法发货");
        }

        if (order.getStatus() != Order.OrderStatus.CUSTOMS_SUCCESS) {
            throw new RuntimeException("订单报关未成功，无法发货");
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            Product product = memoryStore.getProducts().get(item.getProductId());
            if (product != null && product.getWeight() != null) {
                totalWeight = totalWeight.add(product.getWeight().multiply(new BigDecimal(item.getQuantity())));
            }
        }

        Logistics logistics = new Logistics();
        logistics.setId(UUID.randomUUID().toString());
        logistics.setOrderId(orderId);
        logistics.setTrackingNo(generateTrackingNo());
        logistics.setCarrier("国际物流专线");
        logistics.setStatus(Logistics.LogisticsStatus.PENDING);
        logistics.setFromAddress(fromAddress);
        logistics.setToAddress(toAddress);
        logistics.setReceiverName(receiverName);
        logistics.setReceiverPhone(receiverPhone);
        logistics.setWeight(totalWeight);
        logistics.setShippingFee(calculateShippingFee(totalWeight));

        memoryStore.getLogistics().put(logistics.getId(), logistics);

        order.setLogisticsId(logistics.getId());
        order.setStatus(Order.OrderStatus.PENDING_SHIPMENT);
        order.setUpdateTime(LocalDateTime.now());

        logger.info("物流单创建成功, 订单号: {}, 物流单号: {}", order.getOrderNo(), logistics.getTrackingNo());
        return logistics;
    }

    public Logistics shipOut(String logisticsId) {
        Logistics logistics = memoryStore.getLogistics().get(logisticsId);
        if (logistics == null) {
            throw new RuntimeException("物流单不存在");
        }

        if (logistics.getStatus() != Logistics.LogisticsStatus.PENDING) {
            throw new RuntimeException("物流单状态不正确，无法发货");
        }

        Order order = orderService.getOrderById(logistics.getOrderId());
        if (order != null) {
            if (order.getStatus() == Order.OrderStatus.REFUNDING || 
                order.getStatus() == Order.OrderStatus.REFUNDED ||
                order.getStatus() == Order.OrderStatus.CANCELLED) {
                throw new RuntimeException("订单已退款或已取消，无法发货");
            }
        }

        logistics.setStatus(Logistics.LogisticsStatus.SHIPPED);
        logistics.setShipTime(LocalDateTime.now());
        logistics.setUpdateTime(LocalDateTime.now());

        if (order != null) {
            order.setStatus(Order.OrderStatus.SHIPPED);
            order.setUpdateTime(LocalDateTime.now());
        }

        addTrackingLog(logistics.getId(), LogisticsTracking.LogisticsStatus.SHIPPED, 
                "仓库", "货物已出库，开始发货");

        logger.info("货物已发出, 物流单号: {}", logistics.getTrackingNo());
        return logistics;
    }

    public Logistics updateLogisticsStatus(String logisticsId, Logistics.LogisticsStatus status,
                                             String location, String description) {
        Logistics logistics = memoryStore.getLogistics().get(logisticsId);
        if (logistics == null) {
            throw new RuntimeException("物流单不存在");
        }

        logistics.setStatus(status);
        logistics.setUpdateTime(LocalDateTime.now());

        if (status == Logistics.LogisticsStatus.DELIVERED) {
            logistics.setDeliveryTime(LocalDateTime.now());
            Order order = orderService.getOrderById(logistics.getOrderId());
            if (order != null) {
                order.setStatus(Order.OrderStatus.DELIVERED);
                order.setUpdateTime(LocalDateTime.now());
            }
        }

        LogisticsTracking.LogisticsStatus trackingStatus = convertStatus(status);
        addTrackingLog(logisticsId, trackingStatus, location, description);

        logger.info("物流状态更新, 物流单号: {}, 状态: {}", logistics.getTrackingNo(), status.getDescription());
        return logistics;
    }

    public LogisticsTracking addTrackingLog(String logisticsId, LogisticsTracking.LogisticsStatus status,
                                              String location, String description) {
        Logistics logistics = memoryStore.getLogistics().get(logisticsId);
        if (logistics == null) {
            throw new RuntimeException("物流单不存在");
        }

        LogisticsTracking tracking = new LogisticsTracking();
        tracking.setId(UUID.randomUUID().toString());
        tracking.setLogisticsId(logisticsId);
        tracking.setStatus(status);
        tracking.setLocation(location);
        tracking.setDescription(description);
        tracking.setEventTime(LocalDateTime.now());

        memoryStore.getLogisticsTrackings().put(tracking.getId(), tracking);

        List<LogisticsTracking> trackingList = logistics.getTrackingList();
        if (trackingList == null) {
            trackingList = new ArrayList<>();
            logistics.setTrackingList(trackingList);
        }
        trackingList.add(tracking);

        return tracking;
    }

    public Logistics getLogisticsById(String logisticsId) {
        return memoryStore.getLogistics().get(logisticsId);
    }

    public Logistics getLogisticsByOrderId(String orderId) {
        for (Logistics logistics : memoryStore.getAllLogistics()) {
            if (logistics.getOrderId().equals(orderId)) {
                return logistics;
            }
        }
        return null;
    }

    public List<Logistics> getAllLogistics() {
        return memoryStore.getAllLogistics();
    }

    public List<LogisticsTracking> getTrackingList(String logisticsId) {
        List<LogisticsTracking> result = new ArrayList<>();
        for (LogisticsTracking tracking : memoryStore.getLogisticsTrackings().values()) {
            if (tracking.getLogisticsId().equals(logisticsId)) {
                result.add(tracking);
            }
        }
        result.sort((a, b) -> b.getEventTime().compareTo(a.getEventTime()));
        return result;
    }

    private BigDecimal calculateShippingFee(BigDecimal weight) {
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal baseFee = new BigDecimal("50.00");
        BigDecimal perKgFee = new BigDecimal("30.00");
        return baseFee.add(weight.multiply(perKgFee)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private LogisticsTracking.LogisticsStatus convertStatus(Logistics.LogisticsStatus status) {
        switch (status) {
            case SHIPPED:
                return LogisticsTracking.LogisticsStatus.SHIPPED;
            case IN_TRANSIT:
                return LogisticsTracking.LogisticsStatus.IN_TRANSIT;
            case CUSTOMS_CLEARANCE:
                return LogisticsTracking.LogisticsStatus.CUSTOMS_CLEARANCE;
            case LAST_MILE:
                return LogisticsTracking.LogisticsStatus.LAST_MILE;
            case DELIVERED:
                return LogisticsTracking.LogisticsStatus.DELIVERED;
            default:
                return LogisticsTracking.LogisticsStatus.PENDING;
        }
    }

    private String generateTrackingNo() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = LOGISTICS_COUNTER.incrementAndGet();
        return "IL" + datePart + String.format("%08d", sequence);
    }
}
