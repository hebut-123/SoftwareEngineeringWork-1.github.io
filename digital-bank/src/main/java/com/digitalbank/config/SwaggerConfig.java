package com.digitalbank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI digitalBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("数字银行核心业务模拟系统 API")
                        .description("模拟现代数字银行核心业务的分布式系统API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("数字银行开发团队")
                                .email("support@digitalbank.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}