package com.david.interaction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.david.interaction.mapper")
public class AdminInteractionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminInteractionApplication.class, args);
    }
}
