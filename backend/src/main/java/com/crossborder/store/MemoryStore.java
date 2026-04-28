package com.crossborder.store;

import com.crossborder.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemoryStore {

    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, OrderItem> orderItems = new ConcurrentHashMap<>();
    private final Map<String, CustomsDeclaration> customsDeclarations = new ConcurrentHashMap<>();
    private final Map<String, Logistics> logistics = new ConcurrentHashMap<>();
    private final Map<String, LogisticsTracking> logisticsTrackings = new ConcurrentHashMap<>();
    private final Map<String, Refund> refunds = new ConcurrentHashMap<>();

    public Map<String, Product> getProducts() {
        return products;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Order> getOrders() {
        return orders;
    }

    public Map<String, OrderItem> getOrderItems() {
        return orderItems;
    }

    public Map<String, CustomsDeclaration> getCustomsDeclarations() {
        return customsDeclarations;
    }

    public Map<String, Logistics> getLogistics() {
        return logistics;
    }

    public Map<String, LogisticsTracking> getLogisticsTrackings() {
        return logisticsTrackings;
    }

    public Map<String, Refund> getRefunds() {
        return refunds;
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public List<CustomsDeclaration> getAllCustomsDeclarations() {
        return new ArrayList<>(customsDeclarations.values());
    }

    public List<Logistics> getAllLogistics() {
        return new ArrayList<>(logistics.values());
    }

    public List<Refund> getAllRefunds() {
        return new ArrayList<>(refunds.values());
    }

    public void clearAll() {
        products.clear();
        users.clear();
        orders.clear();
        orderItems.clear();
        customsDeclarations.clear();
        logistics.clear();
        logisticsTrackings.clear();
        refunds.clear();
    }
}
