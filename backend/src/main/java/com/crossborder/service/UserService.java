package com.crossborder.service;

import com.crossborder.model.User;
import com.crossborder.store.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private MemoryStore memoryStore;

    @Value("${crossborder.annual-quota:26000}")
    private BigDecimal annualQuota;

    public User createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setAnnualQuota(annualQuota);
        if (user.getUsedQuota() == null) {
            user.setUsedQuota(BigDecimal.ZERO);
        }
        if (user.getIsVerified() == null) {
            user.setIsVerified(false);
        }
        memoryStore.getUsers().put(user.getId(), user);
        logger.info("用户创建成功: {} ({})", user.getRealName(), user.getUsername());
        return user;
    }

    public User getUserById(String userId) {
        return memoryStore.getUsers().get(userId);
    }

    public List<User> getAllUsers() {
        return memoryStore.getAllUsers();
    }

    public User verifyUser(String userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new RuntimeException("用户真实姓名不能为空");
        }
        if (user.getIdCardNumber() == null || user.getIdCardNumber().trim().isEmpty()) {
            throw new RuntimeException("用户身份证号不能为空");
        }
        if (!isValidIdCard(user.getIdCardNumber())) {
            throw new RuntimeException("身份证号格式不正确");
        }
        user.setIsVerified(true);
        logger.info("用户实名认证成功: {}", user.getRealName());
        return user;
    }

    public boolean checkUserVerification(String userId) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }
        return Boolean.TRUE.equals(user.getIsVerified());
    }

    public boolean checkQuota(String userId, BigDecimal amount) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!checkUserVerification(userId)) {
            throw new RuntimeException("用户未实名认证");
        }
        BigDecimal remainingQuota = user.getRemainingQuota();
        return remainingQuota.compareTo(amount) >= 0;
    }

    public void consumeQuota(String userId, BigDecimal amount) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal usedQuota = user.getUsedQuota().add(amount);
        user.setUsedQuota(usedQuota);
        logger.info("用户 {} 消耗额度: {}, 当前已使用额度: {}", 
                user.getRealName(), amount, usedQuota);
    }

    public void restoreQuota(String userId, BigDecimal amount) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal usedQuota = user.getUsedQuota().subtract(amount).max(BigDecimal.ZERO);
        user.setUsedQuota(usedQuota);
        logger.info("用户 {} 恢复额度: {}, 当前已使用额度: {}", 
                user.getRealName(), amount, usedQuota);
    }

    public BigDecimal getRemainingQuota(String userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getRemainingQuota();
    }

    private boolean isValidIdCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        idCard = idCard.trim();
        if (idCard.length() != 15 && idCard.length() != 18) {
            return false;
        }
        if (idCard.length() == 18) {
            try {
                int sum = 0;
                int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                String checkCodes = "10X98765432";
                for (int i = 0; i < 17; i++) {
                    sum += Integer.parseInt(idCard.substring(i, i + 1)) * weights[i];
                }
                char expectedCheckCode = checkCodes.charAt(sum % 11);
                char actualCheckCode = idCard.charAt(17);
                return expectedCheckCode == actualCheckCode;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
