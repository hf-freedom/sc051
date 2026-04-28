package com.crossborder.controller;

import com.crossborder.dto.ResultDTO;
import com.crossborder.model.CustomsDeclaration;
import com.crossborder.service.CustomsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customs")
@CrossOrigin(origins = "*")
public class CustomsController {

    @Autowired
    private CustomsService customsService;

    @GetMapping
    public ResultDTO<List<CustomsDeclaration>> getAllDeclarations() {
        List<CustomsDeclaration> declarations = customsService.getAllDeclarations();
        return ResultDTO.success(declarations);
    }

    @GetMapping("/{id}")
    public ResultDTO<CustomsDeclaration> getDeclarationById(@PathVariable String id) {
        CustomsDeclaration declaration = customsService.getDeclarationById(id);
        if (declaration == null) {
            return ResultDTO.error("报关单不存在");
        }
        return ResultDTO.success(declaration);
    }

    @GetMapping("/order/{orderId}")
    public ResultDTO<CustomsDeclaration> getDeclarationByOrderId(@PathVariable String orderId) {
        CustomsDeclaration declaration = customsService.getDeclarationByOrderId(orderId);
        if (declaration == null) {
            return ResultDTO.error("该订单暂无报关单");
        }
        return ResultDTO.success(declaration);
    }

    @PostMapping("/create/{orderId}")
    public ResultDTO<CustomsDeclaration> createDeclaration(@PathVariable String orderId) {
        try {
            CustomsDeclaration declaration = customsService.createDeclaration(orderId);
            return ResultDTO.success("报关单创建成功", declaration);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/submit/{declarationId}")
    public ResultDTO<CustomsDeclaration> submitDeclaration(@PathVariable String declarationId) {
        try {
            CustomsDeclaration declaration = customsService.submitDeclaration(declarationId);
            return ResultDTO.success("报关单提交成功", declaration);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/process/{declarationId}")
    public ResultDTO<CustomsDeclaration> processDeclaration(@PathVariable String declarationId) {
        try {
            CustomsDeclaration declaration = customsService.processDeclaration(declarationId);
            if (declaration.getStatus() == CustomsDeclaration.CustomsStatus.SUCCESS) {
                return ResultDTO.success("报关成功", declaration);
            } else {
                return ResultDTO.error("报关失败: " + declaration.getFailureReason());
            }
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PostMapping("/retry/{declarationId}")
    public ResultDTO<CustomsDeclaration> retryDeclaration(@PathVariable String declarationId) {
        try {
            CustomsDeclaration declaration = customsService.retryDeclaration(declarationId);
            return ResultDTO.success("报关单重试成功", declaration);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @GetMapping("/failed")
    public ResultDTO<List<CustomsDeclaration>> getFailedDeclarations() {
        List<CustomsDeclaration> declarations = customsService.getFailedDeclarations();
        return ResultDTO.success(declarations);
    }
}
