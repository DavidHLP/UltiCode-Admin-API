package com.david.entity.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token验证请求实体
 * 用于内部服务间的token验证
 * 
 * @author david
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidateRequest {
    /**
     * 需要验证的JWT token
     */
    private String token;
}
