package com.imis.petservicebackend.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

// 处理时间格式问题 配置类
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 日期格式
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 时区
            builder.timeZone(TimeZone.getTimeZone("GMT+8"));
        };
    }
}