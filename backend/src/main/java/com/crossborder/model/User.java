package com.crossborder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {
    private String id;
    private String username;
    private String realName;
    private String idCardNumber;
    private String phone;
    private String email;
    private BigDecimal annualQuota;
    private BigDecimal usedQuota;
    private Boolean isVerified;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public User() {
        this.annualQuota = new BigDecimal("26000.00");
        this.usedQuota = BigDecimal.ZERO;
        this.isVerified = false;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public BigDecimal getRemainingQuota() {
        return annualQuota.subtract(usedQuota);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAnnualQuota() {
        return annualQuota;
    }

    public void setAnnualQuota(BigDecimal annualQuota) {
        this.annualQuota = annualQuota;
    }

    public BigDecimal getUsedQuota() {
        return usedQuota;
    }

    public void setUsedQuota(BigDecimal usedQuota) {
        this.usedQuota = usedQuota;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean verified) {
        isVerified = verified;
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
