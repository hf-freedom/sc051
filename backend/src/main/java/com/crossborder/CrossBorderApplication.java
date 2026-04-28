package com.crossborder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrossBorderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrossBorderApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  跨境电商系统启动成功！");
        System.out.println("  后端服务端口: 8004");
        System.out.println("  API基础路径: http://localhost:8004/api");
        System.out.println("==============================================");
    }
}
