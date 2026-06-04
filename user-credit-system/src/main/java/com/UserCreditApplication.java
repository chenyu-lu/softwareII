package com.credit;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@MapperScan("com.credit.module.*.mapper")
public class UserCreditApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserCreditApplication.class, args);
    }
}

