package com.crossborder.controller;

import com.crossborder.dto.ResultDTO;
import com.crossborder.model.User;
import com.crossborder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResultDTO<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResultDTO.success(users);
    }

    @GetMapping("/{id}")
    public ResultDTO<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResultDTO.error("用户不存在");
        }
        return ResultDTO.success(user);
    }

    @PostMapping
    public ResultDTO<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResultDTO.success("用户创建成功", createdUser);
    }

    @PostMapping("/{id}/verify")
    public ResultDTO<User> verifyUser(@PathVariable String id) {
        try {
            User user = userService.verifyUser(id);
            return ResultDTO.success("实名认证成功", user);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/quota")
    public ResultDTO<QuotaInfo> getQuotaInfo(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResultDTO.error("用户不存在");
            }
            QuotaInfo info = new QuotaInfo();
            info.setUserId(id);
            info.setAnnualQuota(user.getAnnualQuota());
            info.setUsedQuota(user.getUsedQuota());
            info.setRemainingQuota(user.getRemainingQuota());
            return ResultDTO.success(info);
        } catch (RuntimeException e) {
            return ResultDTO.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResultDTO<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResultDTO.error("用户不存在");
        }
        user.setId(id);
        User createdUser = userService.createUser(user);
        return ResultDTO.success("用户更新成功", createdUser);
    }

    public static class QuotaInfo {
        private String userId;
        private BigDecimal annualQuota;
        private BigDecimal usedQuota;
        private BigDecimal remainingQuota;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
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

        public BigDecimal getRemainingQuota() {
            return remainingQuota;
        }

        public void setRemainingQuota(BigDecimal remainingQuota) {
            this.remainingQuota = remainingQuota;
        }
    }
}
