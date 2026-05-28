# 代理配置说明

## 问题描述

国内服务器访问美国资源站API被限制，虽然配置了 sing-box 代理（东京节点），但 Java 应用默认不使用系统代理。

## 解决方案

### 1. 已添加的配置

#### application.yml

```yaml
proxy:
  enabled: true
  host: 127.0.0.1
  port: 7890
  type: http
```

#### ProxyConfig.java

自动在应用启动时设置 JVM 系统代理属性，使所有 HTTP/HTTPS 请求通过代理。

#### Https1080Zyk3CrawlerService.java

- 添加了 `getWithProxy()` 方法
- 替换所有 `HttpUtil.get()` 为 `getWithProxy()`
- 确保所有外部 API 请求都通过代理

### 2. 验证代理是否工作

启动应用后，查看日志确认：

```bash
# 检查代理配置是否生效
curl -x http://127.0.0.1:7890 https://api.yzzy-api.com/inc/api_mac10.php?ac=list&t=1&pg=1
```

### 3. 如果仍然无法访问

#### 检查 sing-box 状态

```bash
./vpn.sh status
```

#### 测试代理连通性

```bash
# 测试代理是否能访问目标API
curl -x http://127.0.0.1:7890 https://api.yzzy-api.com/inc/api_mac10.php?ac=list
```

#### 检查防火墙规则

```bash
# 确保 7890 端口可访问
sudo iptables -L -n | grep 7890
```

#### 查看应用日志

```bash
# 查看是否有代理相关错误
tail -f logs/crawler-service.log | grep -i proxy
```

### 4. 临时禁用代理（调试用）

如果需要临时禁用代理进行测试，修改 `application.yml`：

```yaml
proxy:
  enabled: false  # 改为 false
```

### 5. 注意事项

1. **本地服务不走代理**：配置中已设置 `http.nonProxyHosts`，Nacos、MySQL 等本地服务不会经过代理
2. **代理地址**：确保 `127.0.0.1:7890` 是 sing-box 实际监听的地址
3. **超时时间**：代理可能会增加延迟，当前设置为 60 秒超时
4. **并发控制**：已有线程池和批次控制，避免过多并发请求导致代理被封

### 6. 其他服务的代理配置

如果其他服务也需要代理，可以：

1. 复制 `ProxyConfig.java` 到对应服务
2. 在对应的 `application.yml` 中添加 proxy 配置
3. 修改 HTTP 请求使用代理

## 常见问题

**Q: 代理显示正常但还是访问不了？**
A: 检查以下几点：

- Java 应用是否重启（需要重新加载配置）
- sing-box 是否真的在监听 7890 端口：`netstat -tlnp | grep 7890`
- 代理是否需要认证（当前配置未包含认证）

**Q: 如何验证请求确实走了代理？**
A: 可以在 sing-box 日志中查看连接记录，或者在代理服务器上监控流量。

**Q: 代理速度慢怎么办？**
A:

- 调整超时时间
- 减少并发数（修改 `MAX_CONCURRENT`）
- 增加页间延迟
