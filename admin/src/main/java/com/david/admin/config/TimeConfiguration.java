package com.david.admin.config;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
                log.warn("无效的时区配置 [{}]，将使用系统默认时区 {}", timeZoneId, zone, ex);
            }
        }
        return Clock.system(zone);
    }
}
