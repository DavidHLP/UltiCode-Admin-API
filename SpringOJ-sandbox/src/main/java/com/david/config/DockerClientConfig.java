package com.david.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Docker客户端配置类
 * 提供Docker客户端的统一配置和Bean管理
 */
@Slf4j
@Configuration
@RefreshScope
public class DockerClientConfig {
    
    @Value("${docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;
    
    @Value("${docker.max-connections:100}")
    private int maxConnections;
    
    @Value("${docker.connection.timeout:30}")
    private int connectTimeout;
    
    @Value("${docker.response.timeout:45}")
    private int responseTimeout;

    /**
     * 创建Docker客户端Bean
     * @return DockerClient实例
     */
    @Bean
    public DockerClient dockerClient() {
        log.info("初始化Docker客户端配置: host={}, maxConnections={}, connectTimeout={}s, responseTimeout={}s", 
                dockerHost, maxConnections, connectTimeout, responseTimeout);
        
        // 构建Docker客户端配置
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        // 构建HTTP客户端
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(maxConnections)
                .connectionTimeout(Duration.ofSeconds(connectTimeout))
                .responseTimeout(Duration.ofSeconds(responseTimeout))
                .build();

        // 创建Docker客户端实例
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        
        try {
            // 测试连接
            dockerClient.pingCmd().exec();
            log.info("Docker客户端初始化成功，连接测试通过");
        } catch (Exception e) {
            log.error("Docker客户端连接测试失败", e);
            // 不抛出异常，允许应用启动，但记录错误
        }
        
        return dockerClient;
    }
}
