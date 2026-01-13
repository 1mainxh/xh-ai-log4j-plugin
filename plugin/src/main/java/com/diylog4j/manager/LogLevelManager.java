package com.diylog4j.manager;

import com.diylog4j.config.DynamicLogConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * 日志级别管理器
 */
@Slf4j
@Component
public class LogLevelManager {

    private volatile DynamicLogConfig currentConfig;
    private final Map<String, Pattern> pathPatterns = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> requestCounters = new ConcurrentHashMap<>();

    /**
     * 获取接口的动态日志配置
     */
    public DynamicLogConfig.InterfaceLogConfig getInterfaceConfig(String path, String method) {
        if (currentConfig == null || !currentConfig.isEnabled() || currentConfig.getInterfaces() == null) {
            return null;
        }

        for (DynamicLogConfig.InterfaceLogConfig config : currentConfig.getInterfaces()) {
            if (matchesPath(path, config.getPath()) && 
                (config.getMethod() == null || config.getMethod().equalsIgnoreCase(method))) {
                return config;
            }
        }
        return null;
    }

    /**
     * 检查是否应该应用动态日志级别
     */
    public boolean shouldApplyDynamicLevel(String path, String method) {
        DynamicLogConfig.InterfaceLogConfig config = getInterfaceConfig(path, method);
        if (config == null) {
            return false;
        }

        String key = path + ":" + method;
        AtomicInteger counter = requestCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int currentCount = counter.getAndIncrement();
        
        // 如果达到或超过配置的次数，重置计数器并返回 false
        if (currentCount >= config.getCount()) {
            counter.set(0);
            return false;
        }
        
        return true;
    }

    /**
     * 应用动态日志级别
     */
    public void applyDynamicLevel(String loggerName, String level) {
        try {
            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = loggerContext.getConfiguration();
            
            Level logLevel = Level.toLevel(level, Level.INFO);
            
            // 获取或创建 LoggerConfig
            LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
            if (loggerConfig == null || !loggerConfig.getName().equals(loggerName)) {
                loggerConfig = new LoggerConfig(loggerName, logLevel, true);
                configuration.addLogger(loggerName, loggerConfig);
            } else {
                loggerConfig.setLevel(logLevel);
            }
            
            loggerContext.updateLoggers();
            log.debug("Applied dynamic log level: {} for logger: {}", level, loggerName);
        } catch (Exception e) {
            log.error("Failed to apply dynamic log level", e);
        }
    }

    /**
     * 恢复原始日志级别
     */
    public void restoreOriginalLevel(String loggerName, Level originalLevel) {
        try {
            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = loggerContext.getConfiguration();
            
            LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
            if (loggerConfig != null) {
                loggerConfig.setLevel(originalLevel);
                loggerContext.updateLoggers();
                log.debug("Restored original log level for logger: {}", loggerName);
            }
        } catch (Exception e) {
            log.error("Failed to restore original log level", e);
        }
    }

    /**
     * 更新配置
     */
    public void updateConfig(DynamicLogConfig config) {
        this.currentConfig = config;
        pathPatterns.clear();
        requestCounters.clear();
        log.info("Log level manager config updated");
    }

    /**
     * 获取当前配置
     */
    public DynamicLogConfig getCurrentConfig() {
        return currentConfig;
    }

    /**
     * 路径匹配（支持通配符）
     */
    private boolean matchesPath(String path, String pattern) {
        if (pattern == null || path == null) {
            return false;
        }
        
        if (pattern.equals(path)) {
            return true;
        }
        
        // 支持通配符匹配
        Pattern compiledPattern = pathPatterns.computeIfAbsent(pattern, p -> {
            String regex = p.replace("*", ".*").replace("?", ".");
            return Pattern.compile(regex);
        });
        
        return compiledPattern.matcher(path).matches();
    }

    /**
     * 获取原始日志级别
     */
    public Level getOriginalLevel(String loggerName) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
        return loggerConfig != null ? loggerConfig.getLevel() : Level.INFO;
    }
}
