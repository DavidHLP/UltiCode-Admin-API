package com.david;

import com.david.config.LoadBalancerConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@SpringBootApplication
@LoadBalancerClient(name = "user-service", configuration = LoadBalancerConfiguration.class)
public class ProblemsServiceSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProblemsServiceSpringBootApplication.class, args);
    }
}