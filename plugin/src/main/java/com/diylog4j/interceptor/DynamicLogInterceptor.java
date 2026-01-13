package com.diylog4j.interceptor;

import com.diylog4j.config.DynamicLogConfig;
import com.diylog4j.manager.LogLevelManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Stack;

/**
 * 动态日志拦截器
 */
@Slf4j
@Component
public class DynamicLogInterceptor implements HandlerInterceptor {

    @Autowired
    private LogLevelManager logLevelManager;

    private static final ThreadLocal<Stack<Level>> ORIGINAL_LEVELS = ThreadLocal.withInitial(Stack::new);
    private static final ThreadLocal<Stack<String>> LOGGER_NAMES = ThreadLocal.withInitial(Stack::new);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        DynamicLogConfig.InterfaceLogConfig config = logLevelManager.getInterfaceConfig(path, method);
        
        if (config != null && logLevelManager.shouldApplyDynamicLevel(path, method)) {
            // 保存原始日志级别
            String rootLoggerName = "ROOT";
            Level originalLevel = logLevelManager.getOriginalLevel(rootLoggerName);
            ORIGINAL_LEVELS.get().push(originalLevel);
            LOGGER_NAMES.get().push(rootLoggerName);
            
            // 应用动态日志级别
            logLevelManager.applyDynamicLevel(rootLoggerName, config.getLogLevel());
            
            log.info("Applied dynamic log level: {} for path: {} method: {}", 
                    config.getLogLevel(), path, method);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 恢复原始日志级别
        Stack<Level> levels = ORIGINAL_LEVELS.get();
        Stack<String> loggerNames = LOGGER_NAMES.get();
        
        if (!levels.isEmpty() && !loggerNames.isEmpty()) {
            String loggerName = loggerNames.pop();
            Level originalLevel = levels.pop();
            logLevelManager.restoreOriginalLevel(loggerName, originalLevel);
        }
        
        // 清理 ThreadLocal
        if (levels.isEmpty()) {
            ORIGINAL_LEVELS.remove();
            LOGGER_NAMES.remove();
        }
    }
}
