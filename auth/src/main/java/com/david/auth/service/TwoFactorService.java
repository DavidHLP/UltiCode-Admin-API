package com.david.auth.service;

import cn.hutool.core.codec.Base32;
import cn.hutool.core.util.RandomUtil;
import com.david.auth.entity.UserSecurityProfile;
import com.david.auth.mapper.UserSecurityProfileMapper;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int DEFAULT_DIGITS = 6;
    private static final long TIME_STEP_SECONDS = 30;

    private final UserSecurityProfileMapper userSecurityProfileMapper;
    private final Clock clock;

    public Optional<UserSecurityProfile> findProfile(Long userId) {
        return Optional.ofNullable(userSecurityProfileMapper.selectById(userId));
    }

    @Transactional
    public UserSecurityProfile enableMfa(Long userId) {
        String secret = generateSecret();
        UserSecurityProfile profile =
                userSecurityProfileMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (profile == null) {
            profile = new UserSecurityProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(now);
        }
        profile.setMfaSecret(secret);
        profile.setMfaEnabled(true);
        profile.setUpdatedAt(now);
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(now);
        }
        if (userSecurityProfileMapper.selectById(userId) == null) {
            userSecurityProfileMapper.insert(profile);
        } else {
            userSecurityProfileMapper.updateById(profile);
        }
        log.info("已为用户{}开启二次校验", userId);
        return profile;
    }

    @Transactional
    public void disableMfa(Long userId) {
        UserSecurityProfile profile =
                Optional.ofNullable(userSecurityProfileMapper.selectById(userId))
                        .orElse(null);
        if (profile == null) {
            return;
        }
        profile.setMfaEnabled(false);
        profile.setMfaSecret(null);
        profile.setUpdatedAt(LocalDateTime.now(clock));
        userSecurityProfileMapper.updateById(profile);
        log.info("已为用户{}关闭二次校验", userId);
    }

    public boolean verifyCode(String base32Secret, String code) {
        if (!StringUtils.hasText(base32Secret) || !StringUtils.hasText(code)) {
            return false;
        }
        try {
            long currentBucket = Instant.now(clock).getEpochSecond() / TIME_STEP_SECONDS;
            for (int offset = -1; offset <= 1; offset++) {
                long bucket = currentBucket + offset;
                String expected = generateCode(base32Secret, bucket);
                if (expected.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            log.warn("验证二次验证码失败: {}", ex.getMessage());
            return false;
        }
    }

    public String generateProvisioningUri(String issuer, String accountName, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                urlEncode(issuer), urlEncode(accountName), secret, urlEncode(issuer), DEFAULT_DIGITS, TIME_STEP_SECONDS);
    }

    private String generateSecret() {
        byte[] randomBytes = RandomUtil.randomBytes(20);
        return Base32.encode(randomBytes);
    }

    private String generateCode(String secret, long bucket)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] key = Base32.decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(bucket).array();
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0xF;
        int truncatedHash =
                ((hash[offset] & 0x7F) << 24)
                        | ((hash[offset + 1] & 0xFF) << 16)
                        | ((hash[offset + 2] & 0xFF) << 8)
                        | (hash[offset + 3] & 0xFF);
        int otp = truncatedHash % (int) Math.pow(10, DEFAULT_DIGITS);
        return String.format("%0" + DEFAULT_DIGITS + "d", otp);
    }

    @Transactional
    public void markVerified(Long userId) {
        UserSecurityProfile profile =
                Optional.ofNullable(userSecurityProfileMapper.selectById(userId))
                        .orElse(null);
        if (profile == null) {
            return;
        }
        profile.setLastMfaVerifiedAt(
                LocalDateTime.ofInstant(Instant.now(clock), ZoneId.systemDefault()));
        userSecurityProfileMapper.updateById(profile);
    }

    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return value;
        }
    }
}
