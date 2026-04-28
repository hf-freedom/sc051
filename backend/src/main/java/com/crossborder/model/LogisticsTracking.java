package com.crossborder.model;

import java.time.LocalDateTime;

public class LogisticsTracking {
    private String id;
    private String logisticsId;
    private LogisticsStatus status;
    private String location;
    private String description;
    private LocalDateTime eventTime;
    private LocalDateTime createTime;

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

    public LogisticsTracking() {
        this.createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogisticsId() {
        return logisticsId;
    }

    public void setLogisticsId(String logisticsId) {
        this.logisticsId = logisticsId;
    }

    public LogisticsStatus getStatus() {
        return status;
    }

    public void setStatus(LogisticsStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
