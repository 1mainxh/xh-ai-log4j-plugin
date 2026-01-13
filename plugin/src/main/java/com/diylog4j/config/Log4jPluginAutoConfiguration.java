package com.diylog4j.config;

import com.diylog4j.interceptor.DynamicLogInterceptor;
import com.diylog4j.interceptor.FeignLogInterceptor;
import com.diylog4j.interceptor.RpcLogInterceptor;
import com.diylog4j.manager.LogLevelManager;
import com.diylog4j.manager.LogPathManager;
import com.diylog4j.nacos.NacosConfigListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 日志插件自动配置类
 */
@Configuration
@EnableConfigurationProperties(Log4jPluginProperties.class)
@ConditionalOnProperty(prefix = "xh.log4j.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Log4jPluginAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public LogLevelManager logLevelManager() {
        return new LogLevelManager();
    }

    @Bean
    public LogPathManager logPathManager() {
        return new LogPathManager();
    }

    @Bean
    public DynamicLogInterceptor dynamicLogInterceptor() {
        return new DynamicLogInterceptor();
    }

    @Bean
    public FeignLogInterceptor feignLogInterceptor() {
        return new FeignLogInterceptor();
    }

    @Bean
    public RpcLogInterceptor rpcLogInterceptor() {
        return new RpcLogInterceptor();
    }

    @Bean
    public NacosConfigListener nacosConfigListener() {
        return new NacosConfigListener();
    }
}
