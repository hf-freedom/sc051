package com.crossborder.controller;

import com.crossborder.dto.ResultDTO;
import com.crossborder.model.Logistics;
import com.crossborder.model.LogisticsTracking;
import com.crossborder.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics")
@CrossOrigin(origins = "*")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    @GetMapping
    public ResultDTO<List<Logistics>> getAllLogistics() {
        List<Logistics> logisticsList = logisticsService.getAllLogistics();
        return ResultDTO.success(logisticsList);
    }

    @GetMapping("/{id}")
    public ResultDTO<Logistics> getLogisticsById(@PathVariable String id) {
        Logistics logistics = logisticsService.getLogisticsById(id);
        if (logistics == null) {
            return ResultDTO.error("物流单不存在");
        }
        return ResultDTO.success(logistics);
    }

    @GetMapping("/order/{orderId}")
    public ResultDTO<Logistics> getLogisticsByOrderId(@PathVariable String orderId) {
        Logistics logistics = logisticsService.getLogisticsByOrderId(orderId);
        if (logistics == null) {
            return ResultDTO.error("该订单暂无物流信息");
        }
        return ResultDTO.success(logistics);
    }

    @PostMapping("/create")
    public ResultDTO<Logistics> createShipment(@RequestBody ShipmentRequest request) {
        try {
            Logistics logistics = logisticsService.createShipment(
                    request.getOrderId(),
                    request.getFromAddress(),
                    request.getToAddress(),
                    request.getReceiverName(),
                    request.getReceiverPhone()
            );
            return ResultDTO.success("物流单创建成功", logistics);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/ship/{logisticsId}")
    public ResultDTO<Logistics> shipOut(@PathVariable String logisticsId) {
        try {
            Logistics logistics = logisticsService.shipOut(logisticsId);
            return ResultDTO.success("发货成功", logistics);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/update-status")
    public ResultDTO<Logistics> updateStatus(@RequestBody UpdateStatusRequest request) {
        try {
            Logistics.LogisticsStatus status = Logistics.LogisticsStatus.valueOf(request.getStatus().toUpperCase());
            Logistics logistics = logisticsService.updateLogisticsStatus(
                    request.getLogisticsId(),
                    status,
                    request.getLocation(),
                    request.getDescription()
            );
            return ResultDTO.success("物流状态更新成功", logistics);
        } catch (IllegalArgumentException e) {
            return ResultDTO.error("无效的物流状态");
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @GetMapping("/tracking/{logisticsId}")
    public ResultDTO<List<LogisticsTracking>> getTrackingList(@PathVariable String logisticsId) {
        List<LogisticsTracking> trackingList = logisticsService.getTrackingList(logisticsId);
        return ResultDTO.success(trackingList);
    }

    public static class ShipmentRequest {
        private String orderId;
        private String fromAddress;
        private String toAddress;
        private String receiverName;
        private String receiverPhone;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
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
    }

    public static class UpdateStatusRequest {
        private String logisticsId;
        private String status;
        private String location;
        private String description;

        public String getLogisticsId() {
            return logisticsId;
        }

        public void setLogisticsId(String logisticsId) {
            this.logisticsId = logisticsId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
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
    }
}
