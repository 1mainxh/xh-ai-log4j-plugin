package com.diylog4j.config;

import com.diylog4j.interceptor.DynamicLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@ConditionalOnProperty(prefix = "xh.log4j.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private DynamicLogInterceptor dynamicLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (dynamicLogInterceptor != null) {
            registry.addInterceptor(dynamicLogInterceptor)
                    .addPathPatterns("/**")
                    .order(1);
        }
    }
}
