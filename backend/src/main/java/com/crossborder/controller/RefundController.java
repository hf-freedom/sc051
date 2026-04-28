package com.crossborder.controller;

import com.crossborder.dto.ResultDTO;
import com.crossborder.model.Refund;
import com.crossborder.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "*")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping
    public ResultDTO<List<Refund>> getAllRefunds() {
        List<Refund> refunds = refundService.getAllRefunds();
        return ResultDTO.success(refunds);
    }

    @GetMapping("/{id}")
    public ResultDTO<Refund> getRefundById(@PathVariable String id) {
        Refund refund = refundService.getRefundById(id);
        if (refund == null) {
            return ResultDTO.error("退款单不存在");
        }
        return ResultDTO.success(refund);
    }

    @GetMapping("/order/{orderId}")
    public ResultDTO<List<Refund>> getRefundsByOrderId(@PathVariable String orderId) {
        List<Refund> refunds = refundService.getRefundsByOrderId(orderId);
        return ResultDTO.success(refunds);
    }

    @PostMapping("/apply")
    public ResultDTO<Refund> applyRefund(@RequestBody RefundRequest request) {
        try {
            Refund refund = refundService.applyRefund(request.getOrderId(), request.getReason());
            return ResultDTO.success("退款申请提交成功", refund);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/approve/{refundId}")
    public ResultDTO<Refund> approveRefund(@PathVariable String refundId) {
        try {
            Refund refund = refundService.approveRefund(refundId);
            return ResultDTO.success("退款批准成功", refund);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/reject/{refundId}")
    public ResultDTO<Refund> rejectRefund(@PathVariable String refundId, @RequestBody RejectRequest request) {
        try {
            Refund refund = refundService.rejectRefund(refundId, request.getReason());
            return ResultDTO.success("退款申请已拒绝", refund);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    public static class RefundRequest {
        private String orderId;
        private String reason;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
