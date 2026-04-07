# 阿里云服务器部署完整指南

## 🎯 部署目标
在阿里云ECS Ubuntu服务器上部署动漫平台微服务应用，包含：
- API网关服务 (8080端口)
- Nacos注册中心 (8848端口)  
- MySQL数据库 (3306端口)
- Redis缓存 (6379端口)

---

## 📋 准备工作

### 1. 服务器配置要求
- **操作系统**: Ubuntu 20.04/22.04 LTS
- **CPU**: 至少2核
- **内存**: 至少4GB（推荐8GB）
- **磁盘**: 至少50GB可用空间
- **网络**: 公网IP地址

### 2. 阿里云安全组配置
登录阿里云控制台，配置安全组规则：
```
入方向规则：
- SSH (TCP:22)
- HTTP (TCP:80) - 可选
- HTTPS (TCP:443) - 可选  
- 自定义 TCP (TCP:8080,8848,9848,9849,3306,6379)
```

---

## 🔧 环境搭建

### 1. 系统更新
```bash
# 更新系统软件包
sudo apt update && sudo apt upgrade -y

# 安装基础工具
sudo apt install curl wget vim git unzip -y
```

### 2. 安装Docker
```bash
# 安装Docker官方脚本
curl -fsSL https://get.docker.com | sh

# 验证安装
docker --version

# 添加当前用户到docker组
sudo usermod -aG docker $USER

# 重新登录使权限生效
exit
```

### 3. 安装docker-compose
```bash
# 下载docker-compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 添加执行权限
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 4. 安装JDK（仅首次构建需要）
```bash
# 安装OpenJDK 17
sudo apt install openjdk-17-jdk maven -y

# 验证安装
java --version
mvn --version
```

---

## 🚀 项目部署

### 1. 克隆项目代码
```bash
# 克隆项目到服务器
git clone <你的Git仓库URL> anime-platform
cd anime-platform/deploy
```

### 2. 配置环境变量
创建 `.env` 文件：
```bash
# 创建环境配置文件
cat > .env << EOF
MYSQL_ROOT_PASSWORD=your_strong_password_here
TAG=latest
