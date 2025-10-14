package com.david.auth.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TokenKind {
    ACCESS("access"),
    REFRESH("refresh"),
    API("api");

    @EnumValue
    private final String value;

    TokenKind(String value) {
        this.value = value;
    }

    public boolean isAccess() {
        return this == ACCESS;
    }

    public boolean isRefresh() {
        return this == REFRESH;
    }
}
