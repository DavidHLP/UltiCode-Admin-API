package com.david;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ProblemsServiceSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProblemsServiceSpringBootApplication.class, args);
    }
}