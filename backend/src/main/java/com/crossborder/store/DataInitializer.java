package com.crossborder.store;

import com.crossborder.model.Product;
import com.crossborder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MemoryStore memoryStore;

    @Override
    public void run(String... args) {
        initializeProducts();
        initializeUsers();
    }

    private void initializeProducts() {
        Product product1 = new Product();
        product1.setId(UUID.randomUUID().toString());
        product1.setName("日本进口护肤精华液");
        product1.setSku("SKU-JP-001");
        product1.setOriginCountry("日本");
        product1.setTaxRate(new BigDecimal("0.13"));
        product1.setWeight(new BigDecimal("0.25"));
        product1.setIsBonded(true);
        product1.setPrice(new BigDecimal("588.00"));
        product1.setStock(1000);
        product1.setDescription("日本原装进口，高浓度护肤精华液，保税仓直发");
        memoryStore.getProducts().put(product1.getId(), product1);

        Product product2 = new Product();
        product2.setId(UUID.randomUUID().toString());
        product2.setName("韩国进口面膜套装");
        product2.setSku("SKU-KR-001");
        product2.setOriginCountry("韩国");
        product2.setTaxRate(new BigDecimal("0.13"));
        product2.setWeight(new BigDecimal("0.35"));
        product2.setIsBonded(true);
        product2.setPrice(new BigDecimal("299.00"));
        product2.setStock(2000);
        product2.setDescription("韩国进口补水保湿面膜套装，保税仓发货");
        memoryStore.getProducts().put(product2.getId(), product2);

        Product product3 = new Product();
        product3.setId(UUID.randomUUID().toString());
        product3.setName("美国进口保健品");
        product3.setSku("SKU-US-001");
        product3.setOriginCountry("美国");
        product3.setTaxRate(new BigDecimal("0.13"));
        product3.setWeight(new BigDecimal("0.50"));
        product3.setIsBonded(false);
        product3.setPrice(new BigDecimal("899.00"));
        product3.setStock(500);
        product3.setDescription("美国原装进口营养保健品，直邮模式");
        memoryStore.getProducts().put(product3.getId(), product3);

        Product product4 = new Product();
        product4.setId(UUID.randomUUID().toString());
        product4.setName("法国进口香水");
        product4.setSku("SKU-FR-001");
        product4.setOriginCountry("法国");
        product4.setTaxRate(new BigDecimal("0.13"));
        product4.setWeight(new BigDecimal("0.15"));
        product4.setIsBonded(true);
        product4.setPrice(new BigDecimal("1288.00"));
        product4.setStock(300);
        product4.setDescription("法国原装进口香水，保税仓直发");
        memoryStore.getProducts().put(product4.getId(), product4);

        Product product5 = new Product();
        product5.setId(UUID.randomUUID().toString());
        product5.setName("德国进口厨具套装");
        product5.setSku("SKU-DE-001");
        product5.setOriginCountry("德国");
        product5.setTaxRate(new BigDecimal("0.13"));
        product5.setWeight(new BigDecimal("3.50"));
        product5.setIsBonded(false);
        product5.setPrice(new BigDecimal("2599.00"));
        product5.setStock(200);
        product5.setDescription("德国进口高档厨具套装，直邮配送");
        memoryStore.getProducts().put(product5.getId(), product5);
    }

    private void initializeUsers() {
        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setUsername("zhangsan");
        user1.setRealName("张三");
        user1.setIdCardNumber("110101199001011234");
        user1.setPhone("13800138001");
        user1.setEmail("zhangsan@example.com");
        user1.setIsVerified(true);
        user1.setUsedQuota(BigDecimal.ZERO);
        memoryStore.getUsers().put(user1.getId(), user1);

        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setUsername("lisi");
        user2.setRealName("李四");
        user2.setIdCardNumber("310101199202025678");
        user2.setPhone("13900139002");
        user2.setEmail("lisi@example.com");
        user2.setIsVerified(true);
        user2.setUsedQuota(new BigDecimal("5000.00"));
        memoryStore.getUsers().put(user2.getId(), user2);

        User user3 = new User();
        user3.setId(UUID.randomUUID().toString());
        user3.setUsername("wangwu");
        user3.setRealName("王五");
        user3.setIdCardNumber("440101199503039012");
        user3.setPhone("13700137003");
        user3.setEmail("wangwu@example.com");
        user3.setIsVerified(false);
        user3.setUsedQuota(BigDecimal.ZERO);
        memoryStore.getUsers().put(user3.getId(), user3);
    }
}
