# 动漫平台 - 快速部署指南

## 📋 目录

- [系统要求](#系统要求)
- [一键部署](#一键部署)
- [手动部署](#手动部署)
- [配置说明](#配置说明)
- [服务管理](#服务管理)
- [常见问题](#常见问题)

---

## 系统要求

### 硬件要求
- **CPU**: 4核及以上（推荐8核）
- **内存**: 8GB及以上（推荐16GB）
- **硬盘**: 50GB及以上可用空间

### 软件要求
- **操作系统**: Ubuntu 20.04/22.04/24.04
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Java**: OpenJDK 17
- **Maven**: 3.6+
- **Git**: 2.0+

---

## 一键部署

### 1. 登录服务器

```bash
ssh root@你的服务器IP
```

### 2. 下载部署脚本

```bash
# 方式1: 如果已经clone了项目
cd /opt/anime-platform
sudo bash deploy.sh

# 方式2: 直接下载脚本
wget https://raw.githubusercontent.com/你的仓库/deploy.sh
chmod +x deploy.sh
sudo ./deploy.sh
```

### 3. 等待自动部署

脚本会自动完成以下操作：
- ✅ 检查系统环境和依赖
- ✅ 安装必要依赖（Docker、Java、Maven等）
- ✅ 从Git仓库拉取最新代码
- ✅ 备份旧版本
- ✅ Maven编译打包
- ✅ 构建Docker镜像
- ✅ 启动所有服务
- ✅ 检查服务状态

---

## 手动部署

如果你想手动部署，可以按以下步骤操作：

### 1. 安装基础依赖

```bash
# 更新系统
apt update && apt upgrade -y

# 安装Docker
apt install -y docker.io docker-compose

# 启动Docker
systemctl start docker
systemctl enable docker

# 安装Java 17
apt install -y openjdk-17-jdk

# 安装Maven
apt install -y maven

# 安装Git
apt install -y git
```

### 2. 克隆项目代码

```bash
cd /opt
git clone https://gitee.com/crazy-clown/anime-platform.git
cd anime-platform
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置文件，修改必要的参数
nano .env
```

### 4. 编译项目

```bash
# Maven编译打包
mvn clean package -DskipTests
```

### 5. 构建并启动Docker容器

```bash
# 构建Docker镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

---

## 配置说明

### 环境变量配置 (.env)

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `MYSQL_ROOT_PASSWORD` | MySQL root密码 | 必须修改 |
| `MYSQL_DATABASE` | 数据库名 | animedb |
| `NACOS_USERNAME` | Nacos用户名 | nacos |
| `NACOS_PASSWORD` | Nacos密码 | nacos |
| `JWT_SECRET` | JWT密钥 | 必须修改 |
| `GATEWAY_PORT` | 网关端口 | 8080 |
| `ANIME_SERVICE_PORT` | 动漫服务端口 | 8081 |
| `USER_SERVICE_PORT` | 用户服务端口 | 8001 |
| `VIDEO_SERVICE_PORT` | 视频服务端口 | 8002 |
| `DANMAKU_SERVICE_PORT` | 弹幕服务端口 | 8003 |

### 关键配置文件

各服务的配置文件位于：
```
anime-service/src/main/resources/application.yml
user-service/src/main/resources/application.yml
video-service/src/main/resources/application.yml
danmaku-service/src/main/resources/application.yml
gateway/src/main/resources/application.yml
```

---

## 服务管理

### 查看服务状态

```bash
cd /opt/anime-platform

# 查看所有容器状态
docker-compose ps

# 查看服务资源使用
docker stats
```

### 查看服务日志

```bash
# 查看所有服务实时日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f gateway
docker-compose logs -f anime-service
docker-compose logs -f user-service
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f nacos
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启指定服务
docker-compose restart gateway
```

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（会清空数据）
docker-compose down -v
```

### 更新部署

```bash
# 方式1: 使用部署脚本（推荐）
sudo bash deploy.sh

# 方式2: 手动更新
cd /opt/anime-platform
git pull
mvn clean package -DskipTests
docker-compose build
docker-compose up -d
```

### 备份数据

```bash
# 备份MySQL数据
docker exec anime-mysql mysqldump -u root -p123456 animedb > backup_$(date +%Y%m%d).sql

# 备份整个项目目录
tar -czf anime-platform-backup_$(date +%Y%m%d).tar.gz /opt/anime-platform
```

---

## 常见问题

### 1. 端口被占用

```bash
# 查看端口占用
netstat -tuln | grep 8080
# 或
lsof -i :8080

# 停止占用端口的服务
kill -9 <PID>
```

### 2. Docker镜像构建失败

```bash
# 清理Docker缓存
docker system prune -a

# 重新构建
docker-compose build --no-cache
```

### 3. 服务启动失败

```bash
# 查看服务日志
docker-compose logs [服务名]

# 检查配置文件
cat docker-compose.yml

# 检查环境变量
cat .env
```

### 4. 数据库连接失败

```bash
# 检查MySQL是否启动
docker ps | grep mysql

# 进入MySQL容器
docker exec -it anime-mysql mysql -u root -p

# 查看数据库
show databases;
use animedb;
show tables;
```

### 5. Nacos服务异常

```bash
# 重启Nacos
docker-compose restart nacos

# 查看Nacos日志
docker-compose logs nacos

# 访问Nacos控制台
# http://你的IP:8848/nacos
# 默认账号: nacos / nacos
```

### 6. 内存不足

```bash
# 查看内存使用
free -h

# 查看Docker资源使用
docker stats

# 清理不用的Docker资源
docker system prune -a
```

### 7. 防火墙配置

```bash
# 开放必要端口
ufw allow 8080/tcp   # Gateway
ufw allow 8848/tcp   # Nacos
ufw allow 3306/tcp   # MySQL (可选，仅内网)
ufw allow 6379/tcp   # Redis (可选，仅内网)

# 启用防火墙
ufw enable
```

---

## 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API网关（统一入口） |
| Anime Service | 8081 | 动漫内容管理 |
| User Service | 8001 | 用户管理 |
| Video Service | 8002 | 视频管理 |
| Danmaku Service | 8003 | 实时弹幕 |
| Crawler Service | 8085 | 数据爬虫 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 服务注册与配置中心 |

---

## 访问地址

部署完成后，可以通过以下地址访问：

- **API网关**: `http://服务器IP:8080`
- **Nacos控制台**: `http://服务器IP:8848/nacos` (账号: nacos / nacos)
- **API文档**: `http://服务器IP:8080/doc.html` (Knife4j)

---

## 技术支持

如遇到问题，请查看：
1. 部署日志: `/var/log/anime-platform-deploy.log`
2. 服务日志: `docker-compose logs [服务名]`
3. 项目文档: `README.md`

---

**最后更新**: 2026-04-08
