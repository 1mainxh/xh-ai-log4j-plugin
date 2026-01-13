package com.diylog4j.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.diylog4j.config.DynamicLogConfig;
import com.diylog4j.config.Log4jPluginProperties;
import com.diylog4j.manager.LogLevelManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * Nacos 配置监听器
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "xh.log4j.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NacosConfigListener implements Listener {

    @Autowired(required = false)
    private ConfigService configService;

    @Autowired
    private Log4jPluginProperties properties;

    @Autowired
    private LogLevelManager logLevelManager;

    @Autowired(required = false)
    private com.diylog4j.manager.LogPathManager logPathManager;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        if (configService == null) {
            log.warn("Nacos ConfigService not found, dynamic log configuration will not work");
            return;
        }

        try {
            // 添加配置监听器
            configService.addListener(
                    properties.getNacosDataId(),
                    properties.getNacosGroup(),
                    this
            );

            // 初始化时获取一次配置
            String config = configService.getConfig(
                    properties.getNacosDataId(),
                    properties.getNacosGroup(),
                    5000
            );
            if (config != null && !config.trim().isEmpty()) {
                receiveConfigInfo(config);
            }

            log.info("Nacos config listener initialized for dataId: {}, group: {}", 
                    properties.getNacosDataId(), properties.getNacosGroup());
        } catch (Exception e) {
            log.error("Failed to initialize Nacos config listener", e);
        }
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        try {
            log.info("Received Nacos config update: {}", configInfo);
            
            DynamicLogConfig dynamicConfig = objectMapper.readValue(configInfo, DynamicLogConfig.class);
            
            // 更新日志级别管理器
            logLevelManager.updateConfig(dynamicConfig);
            
            // 更新日志路径配置
            if (logPathManager != null && dynamicConfig.getLogPaths() != null) {
                logPathManager.updateLogPaths(dynamicConfig.getLogPaths());
            }
            
            log.info("Dynamic log config updated successfully");
        } catch (Exception e) {
            log.error("Failed to parse Nacos config", e);
        }
    }

    @Override
    public Executor getExecutor() {
        return null; // 使用默认执行器
    }
}
