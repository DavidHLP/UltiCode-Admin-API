package com.david.interfaces;

import com.david.auth.AuthUserInfo;
import com.david.utils.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 认证服务Feign客户端
 * 用于Gateway调用认证服务进行token验证
 */
@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthFeignClient {
    /**
     * 验证token并获取用户信息
     * 调用认证服务的 GET /validate 接口
     *
     * @param token JWT token
     * @return 用户信息
     */
    @GetMapping("/validate/{token}")
    ResponseResult<AuthUserInfo> loadUserByUsername(@PathVariable("token") String token);
}
