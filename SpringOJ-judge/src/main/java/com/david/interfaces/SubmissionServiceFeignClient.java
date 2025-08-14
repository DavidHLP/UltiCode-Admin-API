package com.david.interfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 提交记录服务Feign客户端
 */
@FeignClient(name = "problems-service", path = "/submissions/api" ,contextId = "submissionServiceFeignClient")
public interface SubmissionServiceFeignClient {
}
