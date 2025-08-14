package com.david.interfaces;


import org.springframework.cloud.openfeign.FeignClient;

/**
 * 题目服务Feign客户端
 */
@FeignClient(name = "problems-service", path = "/problems/api/management")
public interface ProblemServiceFeignClient {
}
