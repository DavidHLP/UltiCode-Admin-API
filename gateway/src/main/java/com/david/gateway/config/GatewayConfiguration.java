package com.david.gateway.config;

import com.david.common.forward.AppProperties;
import java.time.Duration;
import java.util.List;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter(AppProperties appProperties) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> allowedOrigins = appProperties.getAllowedOrigins();
        if (!CollectionUtils.isEmpty(allowedOrigins)) {
            if (allowedOrigins.stream().anyMatch(origin -> origin.contains("*"))) {
                corsConfiguration.setAllowedOriginPatterns(allowedOrigins);
            } else {
                corsConfiguration.setAllowedOrigins(allowedOrigins);
            }
        } else {
            corsConfiguration.setAllowedOriginPatterns(List.of("http://localhost:5173"));
        }
        corsConfiguration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
