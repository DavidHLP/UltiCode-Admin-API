package com.david.auth.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;

@Slf4j
@Configuration
public class TimeConfiguration {

    @Bean
    public Clock clock(@Value("${app.time-zone:}") String timeZoneId) {
        ZoneId zone = ZoneId.systemDefault();
        if (StringUtils.hasText(timeZoneId)) {
            try {
                zone = ZoneId.of(timeZoneId);
            } catch (DateTimeException ex) {
                log.warn("配置的时区 [{}] 无效，使用系统默认时区 {}", timeZoneId, zone, ex);
            }
        }
        log.info("初始化统一时间源，使用时区: {}", zone);
        return Clock.system(zone);
    }
}
