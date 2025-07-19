package com.david.entity.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Serializable {
    private Long id;
    private Long userId;
    private String token;
    private TokenType tokenType;
    private boolean expired;
    private boolean revoked;
}