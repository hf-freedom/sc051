package com.crossborder.service;

import com.crossborder.model.*;
import com.crossborder.store.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomsService.class);
    private static final AtomicLong DECLARATION_COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private MemoryStore memoryStore;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Value("${crossborder.customs-success-rate:0.95}")
    private double customsSuccessRate;

    @Value("${crossborder.customs-retry-max:3}")
    private int maxRetryCount;

    public CustomsDeclaration createDeclaration(String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING_CUSTOMS) {
            throw new RuntimeException("订单状态不正确，无法创建报关单");
        }

        User user = userService.getUserById(order.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!user.getIsVerified()) {
            throw new RuntimeException("用户未实名认证");
        }

        CustomsDeclaration declaration = new CustomsDeclaration();
        declaration.setId(UUID.randomUUID().toString());
        declaration.setDeclarationNo(generateDeclarationNo());
        declaration.setOrderId(orderId);
        declaration.setUserId(order.getUserId());
        declaration.setRealName(user.getRealName());
        declaration.setIdCardNumber(user.getIdCardNumber());
        declaration.setTotalAmount(order.getActualAmount().subtract(order.getTaxAmount()));
        declaration.setTaxAmount(order.getTaxAmount());
        declaration.setStatus(CustomsDeclaration.CustomsStatus.PENDING);
        declaration.setRetryCount(0);

        memoryStore.getCustomsDeclarations().put(declaration.getId(), declaration);

        order.setCustomsDeclarationId(declaration.getId());
        order.setUpdateTime(LocalDateTime.now());

        logger.info("报关单创建成功, 订单号: {}, 报关单号: {}", order.getOrderNo(), declaration.getDeclarationNo());
        return declaration;
    }

    public CustomsDeclaration submitDeclaration(String declarationId) {
        CustomsDeclaration declaration = memoryStore.getCustomsDeclarations().get(declarationId);
        if (declaration == null) {
            throw new RuntimeException("报关单不存在");
        }

        if (declaration.getStatus() != CustomsDeclaration.CustomsStatus.PENDING) {
            throw new RuntimeException("报关单状态不正确，无法提交");
        }

        declaration.setStatus(CustomsDeclaration.CustomsStatus.SUBMITTED);
        declaration.setSubmitTime(LocalDateTime.now());
        declaration.setUpdateTime(LocalDateTime.now());

        Order order = orderService.getOrderById(declaration.getOrderId());
        if (order != null) {
            order.setStatus(Order.OrderStatus.CUSTOMS_PROCESSING);
            order.setUpdateTime(LocalDateTime.now());
        }

        logger.info("报关单提交成功, 报关单号: {}", declaration.getDeclarationNo());
        return declaration;
    }

    public CustomsDeclaration processDeclaration(String declarationId) {
        CustomsDeclaration declaration = memoryStore.getCustomsDeclarations().get(declarationId);
        if (declaration == null) {
            throw new RuntimeException("报关单不存在");
        }

        if (declaration.getStatus() != CustomsDeclaration.CustomsStatus.SUBMITTED &&
            declaration.getStatus() != CustomsDeclaration.CustomsStatus.PROCESSING) {
            throw new RuntimeException("报关单状态不正确，无法处理");
        }

        declaration.setStatus(CustomsDeclaration.CustomsStatus.PROCESSING);
        declaration.setUpdateTime(LocalDateTime.now());

        boolean success = ThreadLocalRandom.current().nextDouble() < customsSuccessRate;

        Order order = orderService.getOrderById(declaration.getOrderId());

        if (success) {
            declaration.setStatus(CustomsDeclaration.CustomsStatus.SUCCESS);
            declaration.setProcessTime(LocalDateTime.now());
            declaration.setUpdateTime(LocalDateTime.now());

            if (order != null) {
                order.setStatus(Order.OrderStatus.CUSTOMS_SUCCESS);
                order.setUpdateTime(LocalDateTime.now());
            }

            logger.info("报关成功, 报关单号: {}", declaration.getDeclarationNo());
        } else {
            declaration.setStatus(CustomsDeclaration.CustomsStatus.FAILED);
            declaration.setProcessTime(LocalDateTime.now());
            declaration.setFailureReason("报关系统审核不通过，请检查报关信息");
            declaration.setUpdateTime(LocalDateTime.now());

            if (order != null) {
                order.setStatus(Order.OrderStatus.CUSTOMS_FAILED);
                order.setUpdateTime(LocalDateTime.now());
            }

            logger.warn("报关失败, 报关单号: {}, 原因: {}", declaration.getDeclarationNo(), declaration.getFailureReason());
        }

        return declaration;
    }

    public CustomsDeclaration retryDeclaration(String declarationId) {
        CustomsDeclaration declaration = memoryStore.getCustomsDeclarations().get(declarationId);
        if (declaration == null) {
            throw new RuntimeException("报关单不存在");
        }

        if (declaration.getStatus() != CustomsDeclaration.CustomsStatus.FAILED) {
            throw new RuntimeException("只有报关失败的订单才能重试");
        }

        if (declaration.getRetryCount() >= maxRetryCount) {
            throw new RuntimeException("已达到最大重试次数，无法继续重试");
        }

        declaration.setRetryCount(declaration.getRetryCount() + 1);
        declaration.setStatus(CustomsDeclaration.CustomsStatus.PENDING);
        declaration.setFailureReason(null);
        declaration.setUpdateTime(LocalDateTime.now());

        Order order = orderService.getOrderById(declaration.getOrderId());
        if (order != null) {
            order.setStatus(Order.OrderStatus.PENDING_CUSTOMS);
            order.setUpdateTime(LocalDateTime.now());
        }

        logger.info("报关单重试提交, 报关单号: {}, 当前重试次数: {}", 
                declaration.getDeclarationNo(), declaration.getRetryCount());
        return declaration;
    }

    public CustomsDeclaration getDeclarationById(String declarationId) {
        return memoryStore.getCustomsDeclarations().get(declarationId);
    }

    public CustomsDeclaration getDeclarationByOrderId(String orderId) {
        for (CustomsDeclaration declaration : memoryStore.getAllCustomsDeclarations()) {
            if (declaration.getOrderId().equals(orderId)) {
                return declaration;
            }
        }
        return null;
    }

    public List<CustomsDeclaration> getAllDeclarations() {
        return memoryStore.getAllCustomsDeclarations();
    }

    public List<CustomsDeclaration> getFailedDeclarations() {
        List<CustomsDeclaration> result = new ArrayList<>();
        for (CustomsDeclaration declaration : memoryStore.getAllCustomsDeclarations()) {
            if (declaration.getStatus() == CustomsDeclaration.CustomsStatus.FAILED &&
                declaration.getRetryCount() < maxRetryCount) {
                result.add(declaration);
            }
        }
        return result;
    }

    private String generateDeclarationNo() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = DECLARATION_COUNTER.incrementAndGet();
        return "CD" + datePart + String.format("%06d", sequence);
    }
}
