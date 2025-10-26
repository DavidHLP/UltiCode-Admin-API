package com.david.auth;

import com.david.auth.config.AppProperties;
import com.david.common.forward.ForwardedSecurityAutoConfiguration;
import com.david.common.security.DownstreamSecurityAutoConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
