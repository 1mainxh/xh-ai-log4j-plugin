# xh-ai-log4j-plugin

基于 Log4j2 和 Nacos 的动态日志配置框架，支持接口级别的动态日志级别控制和远程调用日志追踪。

## 功能特性

1. **从 Nacos 读取框架配置**：支持从 Nacos 配置中心动态读取框架配置，判断框架是否启用
2. **接口级别的动态日志级别**：支持为特定接口配置 n 次的动态日志级别打印（如系统日志级别是 ERROR，动态配置 INFO，则该接口会有 n 次请求打印 INFO 级别日志）
3. **远程调用日志支持**：支持 RPC 和 Feign 远程调用的日志级别动态修改
4. **自动集成**：支持通过 pom 引入，自动集成到 Spring Boot 项目
5. **Java Agent 支持**：支持通过 Java Agent 方式扩展
6. **动态日志路径配置**：支持动态指定日志输出路径和文件

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.diylog4j</groupId>
    <artifactId>plugin-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 配置 Nacos

在 `application.yml` 中配置 Nacos 连接信息：

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: 
        group: DEFAULT_GROUP
        file-extension: yml

xh:
  log4j:
    plugin:
      enabled: true
      nacos-data-id: log4j-plugin-config
      nacos-group: DEFAULT_GROUP
      remote-type: feign  # 或 rpc
      default-log-path: logs
      default-log-file-name: application.log
```

### 3. 在 Nacos 中配置动态日志规则

在 Nacos 配置中心创建配置，DataId: `log4j-plugin-config`，Group: `DEFAULT_GROUP`，配置内容如下（JSON 格式）：

```json
{
  "enabled": true,
  "interfaces": [
    {
      "path": "/api/hello",
      "method": "GET",
      "logLevel": "INFO",
      "count": 5,
      "applyToRemote": true,
      "remoteServiceName": "user-service"
    },
    {
      "path": "/api/user/*",
      "method": "POST",
      "logLevel": "DEBUG",
      "count": 3,
      "applyToRemote": true
    }
  ],
  "logPaths": {
    "ROOT": "logs/application.log",
    "com.diylog4j": "logs/diylog4j.log",
    "feign.user-service": "logs/feign-user-service.log"
  }
}
```

### 4. 配置说明

#### 接口配置参数

- `path`: 接口路径，支持通配符（如 `/api/user/*`）
- `method`: HTTP 方法（GET, POST, PUT, DELETE 等），为空则匹配所有方法
- `logLevel`: 动态日志级别（INFO, DEBUG, WARN, ERROR 等）
- `count`: 需要打印日志的次数（n 次）
- `applyToRemote`: 是否应用到远程调用（RPC/Feign）
- `remoteServiceName`: 远程调用服务名（可选，为空则应用到所有远程调用）

#### 日志路径配置

`logPaths` 字段用于配置不同 Logger 的日志输出路径：
- Key: Logger 名称（如 "ROOT", "com.diylog4j", "feign.user-service"）
- Value: 日志文件路径（相对路径或绝对路径）

## Java Agent 使用

如果需要通过 Java Agent 方式使用，可以在启动时添加 JVM 参数：

```bash
java -javaagent:plugin-0.0.1.jar -jar your-application.jar
```

## 工作原理

1. **配置监听**：框架启动时，会从 Nacos 读取配置并注册监听器，当配置变更时自动更新
2. **请求拦截**：通过 Spring MVC 拦截器拦截 HTTP 请求，根据配置决定是否应用动态日志级别
3. **日志级别切换**：在请求处理前临时修改日志级别，请求处理完成后恢复原始级别
4. **远程调用支持**：通过 Feign 拦截器或 RPC 拦截器，在远程调用时应用相同的日志级别配置
5. **计数控制**：每个接口配置的 `count` 参数控制该接口应用动态日志级别的请求次数

## 注意事项

1. 确保项目中已正确配置 Log4j2
2. Nacos 配置中心需要可访问
3. 配置变更会实时生效，无需重启应用
4. 日志级别切换是线程安全的，使用 ThreadLocal 保证隔离

## 示例

参考 `plugin/src/main/resources/application-example.yml` 和 `plugin/src/main/resources/nacos-config-example.json` 查看完整配置示例。
