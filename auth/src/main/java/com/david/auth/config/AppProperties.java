package com.david.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Getter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Security security = new Security();
    private final Mail mail = new Mail();

    private final List<String> whiteListPaths =
            List.of(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/forgot",
                    "/api/auth/introspect",
                    "/actuator/**");

    @Getter
    @Validated
    public static class Security {
        private final Jwt jwt = new Jwt();
        private final Cookies cookies = new Cookies();
        private List<String> corsAllowedOrigins = List.of("http://localhost:5173");

        @Getter
        @Setter
        @Validated
        public static class Cookies {
            private String refreshTokenName = "cf_refresh_token";
            private String accessTokenName = "cf_access_token";
            private String path = "/";
            private String domain;
            private boolean secure = false;
            private String sameSite = "Lax";
        }

        public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
            this.corsAllowedOrigins =
                    corsAllowedOrigins == null || corsAllowedOrigins.isEmpty()
                            ? List.of("http://localhost:5173")
                            : corsAllowedOrigins;
        }

        @Getter
        @Setter
        @Validated
        public static class Jwt {
            /** 用于签名JWT的HMAC密钥，请妥善保管。 */
            @NotBlank private String secret;

            @NotBlank private String issuer = "codeforge-auth";

            @NotNull private Duration accessTokenTtl = Duration.ofMinutes(15);

            @NotNull private Duration refreshTokenTtl = Duration.ofDays(7);
        }
    }

    @Getter
    @Setter
    @Validated
    public static class Mail {

        @NotBlank private String verificationSubject = "Registration Verification Code";

        @NotBlank
        private String verificationTemplate =
                """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #2c7be5;">Welcome to CodeForge</h2>
                    <p>Your verification code is <strong style="font-size: 20px; letter-spacing: 2px;">%s</strong>.</p>
                    <p>Please complete verification within <strong>%d</strong> minutes. If you did not request this, please ignore this email.</p>
                    <hr style="border:none; border-top:1px solid #e0e0e0; margin: 24px 0;">
                    <p style="font-size: 12px; color: #999999;">This email was sent automatically. Please do not reply.</p>
                  </body>
                </html>
                """;

        @NotNull private Duration verificationCodeTtl = Duration.ofMinutes(10);

        @NotBlank private String passwordResetSubject = "Password Reset Request";

        @NotBlank
        private String passwordResetTemplate =
                """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #2c7be5;">Reset Your Password</h2>
                    <p>We received a request to reset your account password.</p>
                    <p>
                      <a href="%s" style="display:inline-block;padding:12px 20px;background-color:#2c7be5;color:#ffffff;text-decoration:none;border-radius:4px;">
                        Reset Password
                      </a>
                    </p>
                    <p>This link will expire in <strong>%d</strong> minutes. If you did not request a password reset, you can safely ignore this email.</p>
                    <hr style="border:none; border-top:1px solid #e0e0e0; margin: 24px 0;">
                    <p style="font-size: 12px; color: #999999;">This email was sent automatically. Please do not reply.</p>
                  </body>
                </html>
                """;

        @NotBlank
        private String passwordResetUrlTemplate =
                "https://codeforge.example.com/reset-password?token=%s";

        @NotNull private Duration passwordResetTokenTtl = Duration.ofMinutes(30);
    }
}
