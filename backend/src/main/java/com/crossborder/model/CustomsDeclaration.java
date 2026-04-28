package com.crossborder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomsDeclaration {
    private String id;
    private String declarationNo;
    private String orderId;
    private String userId;
    private String realName;
    private String idCardNumber;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private CustomsStatus status;
    private Integer retryCount;
    private String failureReason;
    private LocalDateTime submitTime;
    private LocalDateTime processTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public enum CustomsStatus {
        PENDING("待提交"),
        SUBMITTED("已提交"),
        PROCESSING("处理中"),
        SUCCESS("申报成功"),
        FAILED("申报失败");

        private final String description;

        CustomsStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public CustomsDeclaration() {
        this.status = CustomsStatus.PENDING;
        this.retryCount = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeclarationNo() {
        return declarationNo;
    }

    public void setDeclarationNo(String declarationNo) {
        this.declarationNo = declarationNo;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
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

    public CustomsStatus getStatus() {
        return status;
    }

    public void setStatus(CustomsStatus status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
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
