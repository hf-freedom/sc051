package com.crossborder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Refund {
    private String id;
    private String refundNo;
    private String orderId;
    private String userId;
    private RefundType type;
    private RefundStatus status;
    private Order.OrderStatus originalOrderStatus;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal deductAmount;
    private BigDecimal actualRefundAmount;
    private String deductReason;
    private String reason;
    private Boolean quotaRestored;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public enum RefundType {
        FULL_REFUND("全额退款"),
        PARTIAL_REFUND("部分退款");

        private final String description;

        RefundType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RefundStatus {
        PENDING("待审核"),
        APPROVED("已同意"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        REJECTED("已拒绝");

        private final String description;

        RefundStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public Refund() {
        this.status = RefundStatus.PENDING;
        this.quotaRestored = false;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RefundType getType() {
        return type;
    }

    public void setType(RefundType type) {
        this.type = type;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public Order.OrderStatus getOriginalOrderStatus() {
        return originalOrderStatus;
    }

    public void setOriginalOrderStatus(Order.OrderStatus originalOrderStatus) {
        this.originalOrderStatus = originalOrderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDeductAmount() {
        return deductAmount;
    }

    public void setDeductAmount(BigDecimal deductAmount) {
        this.deductAmount = deductAmount;
    }

    public BigDecimal getActualRefundAmount() {
        return actualRefundAmount;
    }

    public void setActualRefundAmount(BigDecimal actualRefundAmount) {
        this.actualRefundAmount = actualRefundAmount;
    }

    public String getDeductReason() {
        return deductReason;
    }

    public void setDeductReason(String deductReason) {
        this.deductReason = deductReason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getQuotaRestored() {
        return quotaRestored;
    }

    public void setQuotaRestored(Boolean quotaRestored) {
        this.quotaRestored = quotaRestored;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
