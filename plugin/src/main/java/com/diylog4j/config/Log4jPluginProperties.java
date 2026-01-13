package com.diylog4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志框架配置属性
 */
@Data
@ConfigurationProperties(prefix = "xh.log4j.plugin")
public class Log4jPluginProperties {

    /**
     * 是否启用插件
     */
    private boolean enabled = true;

    /**
     * Nacos 配置 DataId
     */
    private String nacosDataId = "log4j-plugin-config";

    /**
     * Nacos 配置 Group
     */
    private String nacosGroup = "DEFAULT_GROUP";

    /**
     * 远程调用类型：rpc 或 feign
     */
    private RemoteType remoteType = RemoteType.FEIGN;

    /**
     * 默认日志输出路径
     */
    private String defaultLogPath = "logs";

    /**
     * 默认日志文件名
     */
    private String defaultLogFileName = "application.log";

    public enum RemoteType {
        RPC, FEIGN
    }
}
