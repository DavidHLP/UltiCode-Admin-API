package com.david;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class JudgeServiceSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeServiceSpringApplication.class, args);
    }
}