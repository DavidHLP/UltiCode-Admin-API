package com.david.common.forward;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** 自动配置类，用于将转发用户基础设施集成到Spring MVC应用程序中。 */
@AutoConfiguration
@ConditionalOnClass(OncePerRequestFilter.class)
public class ForwardedSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ForwardedUserContextFilter forwardedUserContextFilter() {
        return new ForwardedUserContextFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ForwardedUserMethodArgumentResolver forwardedUserMethodArgumentResolver() {
        return new ForwardedUserMethodArgumentResolver();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer forwardedUserWebMvcConfigurer(
            ForwardedUserMethodArgumentResolver resolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(
                    @NonNull List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(resolver);
            }
        };
    }
}
