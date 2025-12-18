package com.digitalbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DigitalBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalBankApplication.class, args);
        System.out.println("数字银行核心业务模拟系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/swagger-ui.html");
    }
}