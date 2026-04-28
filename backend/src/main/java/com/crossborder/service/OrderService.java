package com.crossborder.service;

import com.crossborder.dto.OrderRequestDTO;
import com.crossborder.model.*;
import com.crossborder.store.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final AtomicLong ORDER_COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private MemoryStore memoryStore;

    @Autowired
    private UserService userService;

    @Autowired
    private TaxService taxService;

    public Order createOrder(OrderRequestDTO request) {
        logger.info("创建订单开始, 用户ID: {}", request.getUserId());

        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!user.getIsVerified()) {
            throw new RuntimeException("用户未完成实名认证，无法下单");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;

        for (OrderRequestDTO.OrderItemRequest itemRequest : request.getItems()) {
            Product product = memoryStore.getProducts().get(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + itemRequest.getProductId());
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID().toString());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setSku(product.getSku());
            orderItem.setOriginCountry(product.getOriginCountry());
            orderItem.setTaxRate(product.getTaxRate());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setIsBonded(product.getIsBonded());

            BigDecimal itemTotalAmount = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            orderItem.setTotalAmount(itemTotalAmount);

            BigDecimal itemDiscountAmount = itemRequest.getDiscountAmount() != null 
                    ? itemRequest.getDiscountAmount() 
                    : BigDecimal.ZERO;
            orderItem.setDiscountAmount(itemDiscountAmount);

            BigDecimal itemAfterDiscount = itemTotalAmount.subtract(itemDiscountAmount).max(BigDecimal.ZERO);
            BigDecimal itemTaxAmount = taxService.calculateTax(itemAfterDiscount, product.getTaxRate());
            orderItem.setTaxAmount(itemTaxAmount);
            orderItem.setActualAmount(itemAfterDiscount.add(itemTaxAmount));

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemTotalAmount);
            totalDiscountAmount = totalDiscountAmount.add(itemDiscountAmount);
            totalTaxAmount = totalTaxAmount.add(itemTaxAmount);
        }

        BigDecimal afterDiscountAmount = totalAmount.subtract(totalDiscountAmount).max(BigDecimal.ZERO);
        BigDecimal actualAmount = afterDiscountAmount.add(totalTaxAmount);

        if (!userService.checkQuota(request.getUserId(), actualAmount)) {
            throw new RuntimeException("用户年度跨境额度不足");
        }

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(totalDiscountAmount);
        order.setTaxAmount(totalTaxAmount);
        order.setActualAmount(actualAmount);
        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            memoryStore.getOrderItems().put(item.getId(), item);
        }

        memoryStore.getOrders().put(order.getId(), order);
        logger.info("订单创建成功, 订单号: {}, 金额: {}", order.getOrderNo(), actualAmount);
        return order;
    }

    public Order payOrder(String orderId) {
        Order order = memoryStore.getOrders().get(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }

        User user = userService.getUserById(order.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!userService.checkQuota(order.getUserId(), order.getActualAmount())) {
            throw new RuntimeException("用户年度跨境额度不足");
        }

        userService.consumeQuota(order.getUserId(), order.getActualAmount());

        for (OrderItem item : order.getItems()) {
            Product product = memoryStore.getProducts().get(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() - item.getQuantity());
            }
        }

        order.setPaymentId(UUID.randomUUID().toString());
        order.setPaymentTime(java.time.LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING_CUSTOMS);
        order.setUpdateTime(java.time.LocalDateTime.now());

        logger.info("订单支付成功, 订单号: {}", order.getOrderNo());
        return order;
    }

    public Order getOrderById(String orderId) {
        return memoryStore.getOrders().get(orderId);
    }

    public List<Order> getOrdersByUserId(String userId) {
        List<Order> result = new ArrayList<>();
        for (Order order : memoryStore.getAllOrders()) {
            if (order.getUserId().equals(userId)) {
                result.add(order);
            }
        }
        return result;
    }

    public List<Order> getAllOrders() {
        return memoryStore.getAllOrders();
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> result = new ArrayList<>();
        for (Order order : memoryStore.getAllOrders()) {
            if (order.getStatus() == status) {
                result.add(order);
            }
        }
        return result;
    }

    private String generateOrderNo() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        long sequence = ORDER_COUNTER.incrementAndGet();
        return "CB" + datePart + String.format("%08d", sequence);
    }
}
