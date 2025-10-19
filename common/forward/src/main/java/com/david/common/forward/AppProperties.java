package com.david.common.forward;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> whiteListPaths =
            List.of(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/forgot",
                    "/api/auth/introspect",
                    "/actuator/**");

    private List<String> allowedOrigins = List.of("http://localhost:5173");

    private Duration tokenCacheTtl = Duration.ofSeconds(30);

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins =
                allowedOrigins == null || allowedOrigins.isEmpty()
                        ? List.of("http://localhost:5173")
                        : allowedOrigins;
    }

    public void setTokenCacheTtl(Duration tokenCacheTtl) {
        this.tokenCacheTtl =
                tokenCacheTtl == null || tokenCacheTtl.isNegative() || tokenCacheTtl.isZero()
                        ? Duration.ofSeconds(30)
                        : tokenCacheTtl;
    }
}
