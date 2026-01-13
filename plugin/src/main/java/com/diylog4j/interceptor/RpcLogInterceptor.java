package com.diylog4j.interceptor;

import com.diylog4j.config.DynamicLogConfig;
import com.diylog4j.config.Log4jPluginProperties;
import com.diylog4j.manager.LogLevelManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Stack;

/**
 * RPC 日志拦截器（支持 Dubbo 等 RPC 框架）
 * 注意：这是一个基础实现，实际使用时需要根据具体的 RPC 框架进行适配
 */
@Slf4j
@Component
public class RpcLogInterceptor {

    @Autowired
    private LogLevelManager logLevelManager;

    @Autowired
    private Log4jPluginProperties properties;

    private static final ThreadLocal<Stack<Level>> ORIGINAL_LEVELS = ThreadLocal.withInitial(Stack::new);
    private static final ThreadLocal<Stack<String>> LOGGER_NAMES = ThreadLocal.withInitial(Stack::new);

    /**
     * RPC 调用前拦截
     * @param serviceName 服务名
     * @param methodName 方法名
     */
    public void beforeInvoke(String serviceName, String methodName) {
        // 只在启用 RPC 时应用
        if (properties.getRemoteType() != Log4jPluginProperties.RemoteType.RPC) {
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
            // 检查远程服务名是否匹配
            if (config.getRemoteServiceName() != null && 
                !config.getRemoteServiceName().isEmpty() &&
                !serviceName.equals(config.getRemoteServiceName())) {
                return;
            }

            // 保存原始日志级别
            String loggerName = "rpc." + serviceName;
            Level originalLevel = logLevelManager.getOriginalLevel(loggerName);
            ORIGINAL_LEVELS.get().push(originalLevel);
            LOGGER_NAMES.get().push(loggerName);
            
            // 应用动态日志级别
            logLevelManager.applyDynamicLevel(loggerName, config.getLogLevel());
            
            log.debug("Applied dynamic log level: {} for RPC service: {}", 
                    config.getLogLevel(), serviceName);
        }
    }

    /**
     * RPC 调用后拦截
     */
    public void afterInvoke() {
        Stack<Level> levels = ORIGINAL_LEVELS.get();
        Stack<String> loggerNames = LOGGER_NAMES.get();
        
        if (!levels.isEmpty() && !loggerNames.isEmpty()) {
            String loggerName = loggerNames.pop();
            Level originalLevel = levels.pop();
            logLevelManager.restoreOriginalLevel(loggerName, originalLevel);
        }
        
        if (levels.isEmpty()) {
            ORIGINAL_LEVELS.remove();
            LOGGER_NAMES.remove();
        }
    }
}
