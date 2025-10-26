package com.david.auth.config;

import com.david.core.forward.AppConvention;

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
public class AppProperties extends AppConvention {

    private final Security security = new Security();
    private final Mail mail = new Mail();

    /** 仍然放在根级，名称不变，避免破坏配置兼容性 */
    private final List<String> whiteListPaths = DEFAULT_WHITE_LIST_PATHS;

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

        @NotBlank private String sensitiveActionSubject = "Sensitive Action Verification";

        @NotBlank
        private String sensitiveActionTemplate =
                """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #2c7be5;">Security Verification</h2>
                    <p>We detected a sensitive operation request for your CodeForge account.</p>
                    <p>Your verification code is <strong style="font-size: 20px; letter-spacing: 2px;">%s</strong>.</p>
                    <p>The code is valid for <strong>%d</strong> minutes. If this was not initiated by you, please secure your account.</p>
                    <hr style="border:none; border-top:1px solid #e0e0e0; margin: 24px 0;">
                    <p style="font-size: 12px; color: #999999;">This email was sent automatically. Please do not reply.</p>
                  </body>
                </html>
                """;

        @NotNull private Duration sensitiveActionCodeTtl = Duration.ofMinutes(5);

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

    @Getter
    @Validated
    public class Security {
        private final Jwt jwt = new Jwt();
        private final Cookies cookies = new Cookies();

        /** 此类把 CORS 放在 security 层级，因此字段名不同但默认值仍复用父类常量 */
        private List<String> corsAllowedOrigins = DEFAULT_ALLOWED_ORIGINS;

        public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
            this.corsAllowedOrigins = normalizeList(corsAllowedOrigins);
        }

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
}