package com.crossborder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Logistics {
    private String id;
    private String orderId;
    private String trackingNo;
    private String carrier;
    private LogisticsStatus status;
    private String fromAddress;
    private String toAddress;
    private BigDecimal weight;
    private BigDecimal shippingFee;
    private String receiverName;
    private String receiverPhone;
    private LocalDateTime shipTime;
    private LocalDateTime deliveryTime;
    private List<LogisticsTracking> trackingList = new ArrayList<>();
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public enum LogisticsStatus {
        PENDING("待发货"),
        SHIPPED("已发货"),
        IN_TRANSIT("运输中"),
        CUSTOMS_CLEARANCE("清关中"),
        LAST_MILE("末端派送"),
        DELIVERED("已签收");

        private final String description;

        LogisticsStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public Logistics() {
        this.status = LogisticsStatus.PENDING;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public LogisticsStatus getStatus() {
        return status;
    }

    public void setStatus(LogisticsStatus status) {
        this.status = status;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public LocalDateTime getShipTime() {
        return shipTime;
    }

    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public List<LogisticsTracking> getTrackingList() {
        return trackingList;
    }

    public void setTrackingList(List<LogisticsTracking> trackingList) {
        this.trackingList = trackingList;
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
