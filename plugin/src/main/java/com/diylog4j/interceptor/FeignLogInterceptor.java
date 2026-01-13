package com.diylog4j.interceptor;

import com.diylog4j.config.DynamicLogConfig;
import com.diylog4j.config.Log4jPluginProperties;
import com.diylog4j.manager.LogLevelManager;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Stack;

/**
 * Feign 日志拦截器
 */
@Slf4j
@Component
public class FeignLogInterceptor implements RequestInterceptor {

    @Autowired
    private LogLevelManager logLevelManager;

    @Autowired
    private Log4jPluginProperties properties;

    private static final ThreadLocal<Stack<Level>> ORIGINAL_LEVELS = ThreadLocal.withInitial(Stack::new);
    private static final ThreadLocal<Stack<String>> LOGGER_NAMES = ThreadLocal.withInitial(Stack::new);

    @Override
    public void apply(RequestTemplate template) {
        // 只在启用 Feign 且当前请求有动态日志配置时应用
        if (properties.getRemoteType() != Log4jPluginProperties.RemoteType.FEIGN) {
            return;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String path = request.getRequestURI();
        String method = request.getMethod();

        DynamicLogConfig.InterfaceLogConfig config = logLevelManager.getInterfaceConfig(path, method);
        
        if (config != null && config.isApplyToRemote()) {
            // 获取服务名（从 URL 或配置中提取）
            String serviceName = extractServiceName(template);
            
            // 检查远程服务名是否匹配
            if (config.getRemoteServiceName() != null && 
                !config.getRemoteServiceName().isEmpty() &&
                !serviceName.equals(config.getRemoteServiceName())) {
                return;
            }

            // 保存原始日志级别
            String loggerName = "feign." + serviceName;
            Level originalLevel = logLevelManager.getOriginalLevel(loggerName);
            ORIGINAL_LEVELS.get().push(originalLevel);
            LOGGER_NAMES.get().push(loggerName);
            
            // 应用动态日志级别
            logLevelManager.applyDynamicLevel(loggerName, config.getLogLevel());
            
            log.debug("Applied dynamic log level: {} for Feign service: {}", 
                    config.getLogLevel(), serviceName);
        }
    }

    /**
     * 恢复 Feign 调用的日志级别（需要在调用完成后调用）
     */
    public void restoreLevels() {
        Stack<Level> levels = ORIGINAL_LEVELS.get();
        Stack<String> loggerNames = LOGGER_NAMES.get();
        
        while (!levels.isEmpty() && !loggerNames.isEmpty()) {
            String loggerName = loggerNames.pop();
            Level originalLevel = levels.pop();
            logLevelManager.restoreOriginalLevel(loggerName, originalLevel);
        }
        
        if (levels.isEmpty()) {
            ORIGINAL_LEVELS.remove();
            LOGGER_NAMES.remove();
        }
    }

    /**
     * 从 RequestTemplate 中提取服务名
     */
    private String extractServiceName(RequestTemplate template) {
        // 尝试从 URL 中提取服务名
        String url = template.url();
        if (url != null && url.startsWith("http://")) {
            // 从 URL 中提取主机名作为服务名
            try {
                String host = url.substring(7); // 去掉 "http://"
                int slashIndex = host.indexOf('/');
                if (slashIndex > 0) {
                    host = host.substring(0, slashIndex);
                }
                int colonIndex = host.indexOf(':');
                if (colonIndex > 0) {
                    host = host.substring(0, colonIndex);
                }
                return host;
            } catch (Exception e) {
                // 忽略异常，使用默认值
            }
        }
        
        // 如果无法从 URL 提取，使用默认服务名
        return "default-service";
    }
}
