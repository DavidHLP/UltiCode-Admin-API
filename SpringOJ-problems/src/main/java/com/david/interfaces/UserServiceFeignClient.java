package com.david.interfaces;

import com.david.entity.user.User;
import com.david.utils.ResponseResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", path = "/user/api/user")
public interface UserServiceFeignClient {
	@GetMapping("/ids")
	ResponseResult<List<User>> getUserByIds(@RequestParam List<Long> ids);

	@GetMapping("/id")
	ResponseResult<User> getUserById(@RequestParam Long id);
}
