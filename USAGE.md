# 使用指南

## 1. Maven 依赖配置

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.diylog4j</groupId>
    <artifactId>plugin-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## 2. 应用配置

在 `application.yml` 中添加配置：

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
      remote-type: feign  # 可选值: feign, rpc
      default-log-path: logs
      default-log-file-name: application.log
```

## 3. Nacos 配置

在 Nacos 配置中心创建配置：

- **DataId**: `log4j-plugin-config`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: JSON

配置示例：

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

## 4. 配置说明

### 接口配置参数

| 参数 | 类型 | 说明 | 必填 |
|------|------|------|------|
| path | String | 接口路径，支持通配符（如 `/api/user/*`） | 是 |
| method | String | HTTP 方法（GET, POST, PUT, DELETE 等），为空则匹配所有方法 | 否 |
| logLevel | String | 动态日志级别（INFO, DEBUG, WARN, ERROR 等） | 是 |
| count | Integer | 需要打印日志的次数（n 次） | 是 |
| applyToRemote | Boolean | 是否应用到远程调用（RPC/Feign） | 否 |
| remoteServiceName | String | 远程调用服务名（可选，为空则应用到所有远程调用） | 否 |

### 日志路径配置

`logPaths` 字段用于配置不同 Logger 的日志输出路径：
- Key: Logger 名称（如 "ROOT", "com.diylog4j", "feign.user-service"）
- Value: 日志文件路径（相对路径或绝对路径）

## 5. Java Agent 使用（可选）

如果需要通过 Java Agent 方式使用：

```bash
java -javaagent:plugin-0.0.1.jar -jar your-application.jar
```

## 6. 工作原理

1. **配置监听**：框架启动时，会从 Nacos 读取配置并注册监听器
2. **请求拦截**：通过 Spring MVC 拦截器拦截 HTTP 请求
3. **日志级别切换**：在请求处理前临时修改日志级别，请求处理完成后恢复
4. **远程调用支持**：通过 Feign 拦截器或 RPC 拦截器，在远程调用时应用相同的日志级别配置
5. **计数控制**：每个接口配置的 `count` 参数控制该接口应用动态日志级别的请求次数

## 7. 示例场景

### 场景 1：为特定接口开启 DEBUG 日志

假设系统默认日志级别是 ERROR，现在需要为 `/api/debug` 接口开启 DEBUG 日志，且只打印前 10 次请求：

```json
{
  "enabled": true,
  "interfaces": [
    {
      "path": "/api/debug",
      "method": "GET",
      "logLevel": "DEBUG",
      "count": 10,
      "applyToRemote": false
    }
  ]
}
```

### 场景 2：为接口及其远程调用开启日志

为 `/api/user` 接口及其调用的 `user-service` 服务开启 INFO 日志：

```json
{
  "enabled": true,
  "interfaces": [
    {
      "path": "/api/user",
      "method": "POST",
      "logLevel": "INFO",
      "count": 5,
      "applyToRemote": true,
      "remoteServiceName": "user-service"
    }
  ]
}
```

## 8. 注意事项

1. 确保项目中已正确配置 Log4j2
2. Nacos 配置中心需要可访问
3. 配置变更会实时生效，无需重启应用
4. 日志级别切换是线程安全的，使用 ThreadLocal 保证隔离
5. 计数器是基于接口路径和方法的组合，不同接口的计数器相互独立
