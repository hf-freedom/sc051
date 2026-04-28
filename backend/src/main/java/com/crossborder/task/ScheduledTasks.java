package com.crossborder.task;

import com.crossborder.model.*;
import com.crossborder.service.*;
import com.crossborder.store.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CustomsService customsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemoryStore memoryStore;

    @Scheduled(fixedRate = 60000)
    public void customsRetryTask() {
        logger.info("开始执行报关失败补偿任务...");
        try {
            List<CustomsDeclaration> failedDeclarations = customsService.getFailedDeclarations();
            int retryCount = 0;
            int successCount = 0;

            for (CustomsDeclaration declaration : failedDeclarations) {
                try {
                    logger.info("尝试重试报关单: {}", declaration.getDeclarationNo());
                    
                    CustomsDeclaration retriedDeclaration = customsService.retryDeclaration(declaration.getId());
                    CustomsDeclaration submittedDeclaration = customsService.submitDeclaration(retriedDeclaration.getId());
                    CustomsDeclaration processedDeclaration = customsService.processDeclaration(submittedDeclaration.getId());
                    
                    if (processedDeclaration.getStatus() == CustomsDeclaration.CustomsStatus.SUCCESS) {
                        successCount++;
                        logger.info("报关单重试成功: {}", declaration.getDeclarationNo());
                    }
                    retryCount++;
                } catch (Exception e) {
                    logger.error("报关单重试失败: {}, 错误: {}", declaration.getDeclarationNo(), e.getMessage());
                }
            }

            logger.info("报关失败补偿任务执行完成，处理了 {} 个失败报关单，成功 {} 个", retryCount, successCount);
        } catch (Exception e) {
            logger.error("报关失败补偿任务执行异常", e);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyReconciliationTask() {
        logger.info("开始执行每日对账任务...");
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            BigDecimal totalOrderAmount = BigDecimal.ZERO;
            BigDecimal totalTaxAmount = BigDecimal.ZERO;
            BigDecimal totalPaymentAmount = BigDecimal.ZERO;
            int totalOrders = 0;
            int paidOrders = 0;
            int customsSuccessOrders = 0;
            int customsFailedOrders = 0;
            int shippedOrders = 0;
            int refundedOrders = 0;

            List<Order> orders = orderService.getAllOrders();
            for (Order order : orders) {
                if (order.getCreateTime().isBefore(startOfDay) || 
                    order.getCreateTime().isAfter(endOfDay)) {
                    continue;
                }

                totalOrders++;
                totalOrderAmount = totalOrderAmount.add(order.getTotalAmount());
                totalTaxAmount = totalTaxAmount.add(order.getTaxAmount());

                if (order.getStatus() == Order.OrderStatus.PENDING_CUSTOMS ||
                    order.getStatus() == Order.OrderStatus.CUSTOMS_PROCESSING ||
                    order.getStatus() == Order.OrderStatus.CUSTOMS_SUCCESS ||
                    order.getStatus() == Order.OrderStatus.CUSTOMS_FAILED ||
                    order.getStatus() == Order.OrderStatus.PENDING_SHIPMENT ||
                    order.getStatus() == Order.OrderStatus.SHIPPED ||
                    order.getStatus() == Order.OrderStatus.DELIVERED ||
                    order.getStatus() == Order.OrderStatus.REFUNDED) {
                    paidOrders++;
                    totalPaymentAmount = totalPaymentAmount.add(order.getActualAmount());
                }

                if (order.getStatus() == Order.OrderStatus.CUSTOMS_SUCCESS) {
                    customsSuccessOrders++;
                }

                if (order.getStatus() == Order.OrderStatus.CUSTOMS_FAILED) {
                    customsFailedOrders++;
                }

                if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                    order.getStatus() == Order.OrderStatus.DELIVERED) {
                    shippedOrders++;
                }

                if (order.getStatus() == Order.OrderStatus.REFUNDED) {
                    refundedOrders++;
                }
            }

            logger.info("========== 每日对账单 - {} ==========", today);
            logger.info("总订单数: {}", totalOrders);
            logger.info("已支付订单数: {}", paidOrders);
            logger.info("总订单金额: {}", totalOrderAmount);
            logger.info("总税费金额: {}", totalTaxAmount);
            logger.info("总支付金额: {}", totalPaymentAmount);
            logger.info("报关成功订单数: {}", customsSuccessOrders);
            logger.info("报关失败订单数: {}", customsFailedOrders);
            logger.info("已发货订单数: {}", shippedOrders);
            logger.info("已退款订单数: {}", refundedOrders);
            logger.info("==================================================");

            ReconciliationRecord record = new ReconciliationRecord();
            record.setDate(today);
            record.setTotalOrders(totalOrders);
            record.setPaidOrders(paidOrders);
            record.setTotalOrderAmount(totalOrderAmount);
            record.setTotalTaxAmount(totalTaxAmount);
            record.setTotalPaymentAmount(totalPaymentAmount);
            record.setCustomsSuccessOrders(customsSuccessOrders);
            record.setCustomsFailedOrders(customsFailedOrders);
            record.setShippedOrders(shippedOrders);
            record.setRefundedOrders(refundedOrders);
            record.setCreateTime(LocalDateTime.now());

            logger.info("每日对账任务执行完成");
        } catch (Exception e) {
            logger.error("每日对账任务执行异常", e);
        }
    }

    public static class ReconciliationRecord {
        private LocalDate date;
        private Integer totalOrders;
        private Integer paidOrders;
        private BigDecimal totalOrderAmount;
        private BigDecimal totalTaxAmount;
        private BigDecimal totalPaymentAmount;
        private Integer customsSuccessOrders;
        private Integer customsFailedOrders;
        private Integer shippedOrders;
        private Integer refundedOrders;
        private LocalDateTime createTime;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Integer getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(Integer totalOrders) {
            this.totalOrders = totalOrders;
        }

        public Integer getPaidOrders() {
            return paidOrders;
        }

        public void setPaidOrders(Integer paidOrders) {
            this.paidOrders = paidOrders;
        }

        public BigDecimal getTotalOrderAmount() {
            return totalOrderAmount;
        }

        public void setTotalOrderAmount(BigDecimal totalOrderAmount) {
            this.totalOrderAmount = totalOrderAmount;
        }

        public BigDecimal getTotalTaxAmount() {
            return totalTaxAmount;
        }

        public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
            this.totalTaxAmount = totalTaxAmount;
        }

        public BigDecimal getTotalPaymentAmount() {
            return totalPaymentAmount;
        }

        public void setTotalPaymentAmount(BigDecimal totalPaymentAmount) {
            this.totalPaymentAmount = totalPaymentAmount;
        }

        public Integer getCustomsSuccessOrders() {
            return customsSuccessOrders;
        }

        public void setCustomsSuccessOrders(Integer customsSuccessOrders) {
            this.customsSuccessOrders = customsSuccessOrders;
        }

        public Integer getCustomsFailedOrders() {
            return customsFailedOrders;
        }

        public void setCustomsFailedOrders(Integer customsFailedOrders) {
            this.customsFailedOrders = customsFailedOrders;
        }

        public Integer getShippedOrders() {
            return shippedOrders;
        }

        public void setShippedOrders(Integer shippedOrders) {
            this.shippedOrders = shippedOrders;
        }

        public Integer getRefundedOrders() {
            return refundedOrders;
        }

        public void setRefundedOrders(Integer refundedOrders) {
            this.refundedOrders = refundedOrders;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}
