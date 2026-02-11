package com.tonvq.accountingerp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot Application Entry Point
 * 
 * Accounting ERP System
 * - Xây dựng theo kiến trúc Domain-Driven Design (DDD)
 * - Spring Boot 3.3+ với OpenJDK 21
 * - PostgreSQL 16+ cho production, H2 cho dev/test
 * - Thymeleaf template engine cho frontend
 * - Bootstrap 5 + Chart.js cho UI/UX
 * 
 * Tuân thủ: Thông tư 99/2025/TT-BTC về kế toán
 * 
 * @author Ton VQ
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.tonvq.accountingerp"})
@OpenAPIDefinition(
    info = @Info(
        title = "Accounting ERP API",
        version = "1.0.0",
        description = "Enterprise Resource Planning System for Accounting",
        contact = @Contact(
            name = "Ton VQ Development Team",
            email = "info@tonvq.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server"),
        @Server(url = "https://api.accounting-erp.com", description = "Production Server")
    }
)
public class AccountingERPApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingERPApplication.class, args);
    }

}
