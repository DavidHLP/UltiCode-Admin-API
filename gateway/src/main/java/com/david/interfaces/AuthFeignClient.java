package com.david.interfaces;

import com.david.entity.token.TokenValidateRequest;
import com.david.entity.user.AuthUser;
import com.david.utils.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 认证服务Feign客户端
 * 用于Gateway调用认证服务进行token验证
 */
@FeignClient(name = "authentication", path = "/api/auth")
public interface AuthFeignClient {
    /**
     * 验证token并获取用户信息
     * 调用认证服务的 POST /validate 接口
     *
     * @param token JWT token
     * @return 用户信息
     */
    @PostMapping("/validate")
    ResponseResult<AuthUser> loadUserByUsername(@RequestBody TokenValidateRequest token);
}
