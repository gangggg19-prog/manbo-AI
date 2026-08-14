package com.mimo.babyassistantserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * 育儿助手后端启动入口。
 * Spring Boot 从该类所在包向下扫描 Controller、Service、Mapper 等组件。
 */
public class BabyAssistantServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabyAssistantServerApplication.class, args);
    }
}