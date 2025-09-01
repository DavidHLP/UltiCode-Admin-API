package com.david.commons.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 测试应用程序启动类
 * 用于Redis测试的Spring Boot配置
 *
 * @author David
 */
@SpringBootApplication
@Import(RealRedisTestBase.RealRedisTestConfiguration.class)
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
