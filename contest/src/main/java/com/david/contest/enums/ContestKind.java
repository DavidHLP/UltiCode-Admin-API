package com.david.contest.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum ContestKind {
    ICPC("icpc", "ICPC"),
    OI("oi", "OI"),
    IOI("ioi", "IOI"),
    CF("cf", "Codeforces"),
    ACM("acm", "ACM"),
    CUSTOM("custom", "自定义");

    private final String code;
    private final String displayName;

    ContestKind(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ContestKind fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("比赛类型不能为空");
        }
        String normalized = code.toLowerCase(Locale.ROOT);
        return Optional.of(normalized)
                .flatMap(
                        value ->
                                Arrays.stream(values())
                                        .filter(item -> item.code.equals(value))
                                        .findFirst())
                .orElseThrow(() -> new IllegalArgumentException("未知的比赛类型: " + code));
    }

    public static Set<String> allCodes() {
        return Arrays.stream(values()).map(ContestKind::getCode).collect(java.util.stream.Collectors.toSet());
    }
}
