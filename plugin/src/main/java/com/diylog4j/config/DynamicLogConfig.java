package com.diylog4j.config;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 动态日志配置模型
 */
@Data
public class DynamicLogConfig {

    /**
     * 是否启用框架
     */
    private boolean enabled = true;

    /**
     * 接口日志配置列表
     */
    private List<InterfaceLogConfig> interfaces;

    /**
     * 日志输出路径配置
     */
    private Map<String, String> logPaths;

    /**
     * 接口日志配置
     */
    @Data
    public static class InterfaceLogConfig {
        /**
         * 接口路径（支持通配符）
         */
        private String path;

        /**
         * HTTP 方法（GET, POST, PUT, DELETE 等，为空则匹配所有方法）
         */
        private String method;

        /**
         * 动态日志级别（INFO, DEBUG, WARN, ERROR 等）
         */
        private String logLevel;

        /**
         * 需要打印日志的次数（n 次）
         */
        private int count = 1;

        /**
         * 是否应用到远程调用（RPC/Feign）
         */
        private boolean applyToRemote = true;

        /**
         * 远程调用服务名（可选，为空则应用到所有远程调用）
         */
        private String remoteServiceName;
    }
}
