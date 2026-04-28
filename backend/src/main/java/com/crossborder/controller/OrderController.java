package com.crossborder.controller;

import com.crossborder.dto.OrderRequestDTO;
import com.crossborder.dto.ResultDTO;
import com.crossborder.model.Order;
import com.crossborder.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResultDTO<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResultDTO.success(orders);
    }

    @GetMapping("/{id}")
    public ResultDTO<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResultDTO.error("订单不存在");
        }
        return ResultDTO.success(order);
    }

    @GetMapping("/user/{userId}")
    public ResultDTO<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResultDTO.success(orders);
    }

    @PostMapping
    public ResultDTO<Order> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        try {
            Order order = orderService.createOrder(request);
            return ResultDTO.success("订单创建成功", order);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay")
    public ResultDTO<Order> payOrder(@PathVariable String id) {
        try {
            Order order = orderService.payOrder(id);
            return ResultDTO.success("支付成功", order);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResultDTO<List<Order>> getOrdersByStatus(@PathVariable String status) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            return ResultDTO.success(orders);
        } catch (IllegalArgumentException e) {
            return ResultDTO.error("无效的订单状态");
        }
    }
}
