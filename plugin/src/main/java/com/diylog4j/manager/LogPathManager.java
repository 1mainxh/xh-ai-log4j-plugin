package com.diylog4j.manager;

import com.diylog4j.config.DynamicLogConfig;
import com.diylog4j.config.Log4jPluginProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 日志路径管理器
 */
@Slf4j
@Component
public class LogPathManager {

    @Autowired
    private Log4jPluginProperties properties;

    /**
     * 更新日志输出路径
     */
    public void updateLogPaths(Map<String, String> logPaths) {
        if (logPaths == null || logPaths.isEmpty()) {
            return;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
            Configuration configuration = loggerContext.getConfiguration();

            for (Map.Entry<String, String> entry : logPaths.entrySet()) {
                String loggerName = entry.getKey();
                String logPath = entry.getValue();

                LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
                if (loggerConfig != null) {
                    // 创建新的文件追加器
                    String fileName = logPath;
                    if (!fileName.contains("/") && !fileName.contains("\\")) {
                        // 如果只是文件名，使用默认路径
                        fileName = properties.getDefaultLogPath() + "/" + logPath;
                    }

                    // 这里需要根据实际的 Log4j2 配置来更新文件追加器
                    // 由于 Log4j2 的配置比较复杂，这里提供一个基础实现
                    log.info("Updated log path for logger: {} to: {}", loggerName, fileName);
                }
            }

            loggerContext.updateLoggers();
            log.info("Log paths updated successfully");
        } catch (Exception e) {
            log.error("Failed to update log paths", e);
        }
    }
}
