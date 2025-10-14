package com.david.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Security security = new Security();

    @Getter
    @Validated
    public static class Security {
        private final Jwt jwt = new Jwt();

        @Getter
        @Setter
        @Validated
        public static class Jwt {
            /** HMAC secret for signing JWTs. Keep this safe. */
            @NotBlank private String secret;

            @NotBlank private String issuer = "codeforge-auth";

            @NotNull private Duration accessTokenTtl = Duration.ofMinutes(15);

            @NotNull private Duration refreshTokenTtl = Duration.ofDays(7);
        }
    }
}
