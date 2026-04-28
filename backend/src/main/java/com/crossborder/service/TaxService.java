package com.crossborder.service;

import com.crossborder.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxService {

    @Value("${crossborder.default-tax-rate:0.13}")
    private BigDecimal defaultTaxRate;

    public BigDecimal calculateTax(BigDecimal amount, BigDecimal taxRate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = taxRate != null ? taxRate : defaultTaxRate;
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(Product product, BigDecimal unitPrice, Integer quantity, BigDecimal discountAmount) {
        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal discountedAmount = discountAmount != null 
                ? totalAmount.subtract(discountAmount).max(BigDecimal.ZERO) 
                : totalAmount;
        return calculateTax(discountedAmount, product.getTaxRate());
    }

    public boolean isTaxExempt(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
