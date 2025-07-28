package com.david;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class SandBoxSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandBoxSpringApplication.class, args);
    }
}