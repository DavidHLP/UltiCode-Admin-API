package com.david.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<String> whiteListPaths =
            List.of(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/actuator/**");

    public List<String> getWhiteListPaths() {
        return whiteListPaths;
    }

    public void setWhiteListPaths(List<String> whiteListPaths) {
        this.whiteListPaths = whiteListPaths;
    }
}
