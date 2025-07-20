package com.david;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.david.mapper")
@EnableAsync
public class JudgeServiceSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeServiceSpringApplication.class, args);
    }
}