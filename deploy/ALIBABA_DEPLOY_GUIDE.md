# 阿里云服务器部署指南（Ubuntu）

## 1. 服务器环境准备

### 1.1 系统要求
- Ubuntu 20.04/22.04 LTS
- 内存：至少4GB（推荐8GB）
- 磁盘：至少50GB可用空间
- CPU：2核以上

### 1.2 安装必要软件

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装Git
sudo apt install git -y

# 安装Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 安装docker-compose
sudo apt install docker-compose -y

# 重新登录使权限生效
exit
```

### 1.3 配置安全组
在阿里云控制台：
- 开放端口：8080, 8848, 9848, 9849, 3306, 6379
- SSH端口：22（确保已开放）

## 2. 项目部署

### 2.1 克隆项目代码

```bash
# 克隆项目到服务器
git clone <你的项目仓库URL> anime-platform
cd anime-platform/deploy
```

### 2.2 配置环境变量

创建 `.env` 文件：
```bash
cat > .env << EOF
MYSQL_ROOT_PASSWORD=your_secure_password_here
TAG=latest
