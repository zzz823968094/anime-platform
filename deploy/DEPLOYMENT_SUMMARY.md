# 动漫平台Docker部署完整指南

## 项目结构说明

这是一个Spring Cloud微服务架构的动漫平台，包含以下服务：

### 核心服务
- **gateway**: API网关服务 (端口: 8080)
- **user-service**: 用户管理服务 (端口: 8001)
- **anime-service**: 动漫内容服务 (端口: 8002)
- **video-service**: 视频服务 (端口: 8003)
- **danmaku-service**: 弹幕服务 (端口: 8004)

### 基础服务
- **MySQL 8.0**: 主数据库 (端口: 3306)
- **Redis 7**: 缓存服务 (端口: 6379)
- **Nacos 2.2.3**: 服务注册与配置中心 (端口: 8848,9848,9849)

## 部署文件清单

### 主要部署文件
1. `docker-compose.prod.yml` - 生产环境docker-compose配置
2. `deploy.sh` - 完整部署脚本
3. `README.md` - 详细部署说明
4. `ALIBABA_DEPLOY_GUIDE.md` - 阿里云服务器专用指南
5. `DEPLOYMENT_SUMMARY.md` - 本文件

### Dockerfile文件
- `Dockerfile.gateway`

## 快速开始

### 本地测试部署

```bash
cd deploy
chmod +x *.sh
./deploy.sh
```

### 阿里云服务器部署

1. **准备服务器**
   ```bash
   # 安装依赖
   sudo apt update && sudo apt install git docker docker-compose -y
   ```

2. **克隆并部署**
   ```bash
   git clone <你的仓库URL> anime-platform
   cd anime-platform/deploy
   chmod +x *.sh
   ./deploy.sh
   ```

## 服务端口映射

| 服务 | 容器端口 | 主机端口 | 访问地址 |
|------|----------|----------|----------|
| 应用网关 | 8080 | 8080 | http://服务器IP:8080 |
| Nacos控制台 | 8848 | 8848 | http://服务器IP:8848/nacos |
| MySQL数据库 | 3306 | 3306 | jdbc:mysql://服务器IP:3306/animedb |
| Redis缓存 | 6379 | 6379 | redis://服务器IP:6379 |

## 常用命令

### 部署操作
```bash
# 首次部署
./deploy.sh

# 更新应用
git pull origin master && ./deploy.sh

# 停止服务
# 创建stop.sh脚本或手动执行docker-compose down
```

### Docker管理
```bash
# 查看运行状态
docker ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f gateway

# 进入容器
docker exec -it anime-gateway-prod bash
```

## 配置文件

### .env 环境变量
```env
MYSQL_ROOT_PASSWORD=your_secure_password
TAG=latest
```

## 故障排除

### 常见问题及解决方案

#### 1. 容器启动失败
```bash
# 查看详细错误
docker-compose -f docker-compose.prod.yml logs <service-name>

# 检查端口冲突
netstat -tlnp | grep :8080
```

#### 2. 网络连接问题
```bash
# 测试服务连通性
curl http://localhost:8080/api/health

# 检查防火墙
sudo ufw status
```

## 安全建议

### 1. 密码安全
- 修改默认MySQL密码
- 定期更换密码
- 使用强密码策略

### 2. 网络安全
```bash
# 只开放必要端口
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8080/tcp   # 应用
sudo ufw enable
```

## 性能优化

### JVM调优
在JAR包启动参数中添加：
```bash
-Xms512m -Xmx1024m -XX:+UseG1GC
```

## 联系支持

如果遇到问题，请提供以下信息：
1. 错误日志
2. 系统资源使用情况
3. 网络连接状态
4. Docker版本信息
