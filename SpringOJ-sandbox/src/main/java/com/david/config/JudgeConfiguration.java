package com.david.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.david.chain.JudgeChainBuilder;
import com.david.chain.UniversalJudgeSandbox;
import com.david.strategy.LanguageStrategyFactory;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

/**
 * 判题系统配置
 */
@Configuration
@EnableConfigurationProperties(JudgeProperties.class)
public class JudgeConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DockerClient dockerClient() {
        var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    @Bean
    public UniversalJudgeSandbox universalJudgeSandbox(
            JudgeChainBuilder chainBuilder,
            DockerClient dockerClient,
            LanguageStrategyFactory strategyFactory) {
        return new UniversalJudgeSandbox(chainBuilder, dockerClient, strategyFactory);
    }
}
