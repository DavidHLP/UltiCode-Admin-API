package com.david.problem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.david.problem.mapper")
public class AdminProblemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminProblemApplication.class, args);
    }
}
