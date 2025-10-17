package com.david.common.forward;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
}
