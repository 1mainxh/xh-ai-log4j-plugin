package com.diylog4j.config;

import com.diylog4j.interceptor.FeignLogInterceptor;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类
 */
@Configuration
@ConditionalOnClass(FeignClient.class)
@ConditionalOnProperty(prefix = "xh.log4j.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FeignConfig {

    @Autowired
    private FeignLogInterceptor feignLogInterceptor;

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return feignLogInterceptor;
    }
}
