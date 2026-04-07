# 动漫平台Docker部署指南

## 环境要求

- Ubuntu 20.04/22.04 LTS
- Docker 20.10+
- docker-compose 2.0+
- Git
- JDK 17（仅首次构建需要）

## 快速开始

### 1. 克隆代码到服务器

```bash
git clone <your-repo-url> anime-platform
cd anime-platform/deploy
```

### 2. 配置环境变量（可选）

创建 `.env` 文件：
```bash
echo "MYSQL_ROOT_PASSWORD=anime123" > .env
```

### 3. 启动服务

```bash
chmod +x *.sh
./deploy.sh
```

## 脚本说明

### `deploy.sh`
完整的部署流程，包括：
- 检查依赖
- 构建服务
- 启动容器
- 验证状态

## 服务端口映射

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|----------|----------|------|
| 网关 | 8080 | 8080 | 应用入口 |
| Nacos | 8848,9848,9849 | 8848,9848,9849 | 注册中心 |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |

## 访问地址

- **应用网关**: http://<服务器IP>:8080
- **Nacos控制台**: http://<服务器IP>:8848/nacos/
  - 用户名: nacos
  - 密码: nacos
- **MySQL**: localhost:3306
  - 用户名: root
  - 密码: anime123（可在.env文件中修改）

## 常见问题

### Q: 服务启动失败怎么办？
A: 查看日志
```bash
docker-compose -f docker-compose.prod.yml logs <service-name>
```

### Q: 如何进入容器调试？
A:
```bash
docker exec -it anime-gateway-prod bash
```

## 注意事项

1. 生产环境建议修改默认密码
2. 数据卷会持久化保存，升级时不会丢失数据
3. 所有服务都配置了自动重启策略
