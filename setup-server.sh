#!/bin/bash

################################################################################
# 动漫平台 - 服务器初始化脚本
# 用途: 在新服务器上一次性安装所有必要的环境和依赖
################################################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}=========================================="
echo -e "${GREEN}动漫平台 - 服务器环境初始化"
echo -e "${BLUE}==========================================${NC}"
echo ""

# 检查root权限
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}✗ 请使用sudo运行此脚本${NC}"
    exit 1
fi

# 生成强密码函数
generate_password() {
    local length=$1
    cat /dev/urandom | tr -dc 'a-zA-Z0-9!@#$%^&*' | fold -w $length | head -n 1
}

# 生成强密码
MYSQL_ROOT_PASSWORD=$(generate_password 20)
MYSQL_PASSWORD=$(generate_password 20)
REDIS_PASSWORD=$(generate_password 20)
NACOS_PASSWORD=$(generate_password 20)
JWT_SECRET=$(generate_password 44)

# 更新系统
echo -e "${YELLOW}[1/9] 更新系统包...${NC}"
apt update && apt upgrade -y

# 安装基础工具
echo -e "${YELLOW}[2/9] 安装基础工具...${NC}"
apt install -y git curl wget unzip net-tools vim

# 安装Docker
echo -e "${YELLOW}[3/9] 安装Docker...${NC}"
if ! command -v docker &> /dev/null; then
    apt install -y docker.io
    systemctl start docker
    systemctl enable docker
    echo -e "${GREEN}✓ Docker安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker已安装${NC}"
fi

# 安装Docker Compose V2
echo -e "${YELLOW}[4/9] 安装Docker Compose V2...${NC}"
if ! docker compose version &> /dev/null; then
    DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
    mkdir -p $DOCKER_CONFIG/cli-plugins
    curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o $DOCKER_CONFIG/cli-plugins/docker-compose
    chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose
    echo -e "${GREEN}✓ Docker Compose V2安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker Compose V2已安装${NC}"
fi

# 安装Java 17
echo -e "${YELLOW}[5/9] 安装Java 17...${NC}"
if ! command -v java &> /dev/null; then
    apt install -y openjdk-17-jdk
    echo -e "${GREEN}✓ Java安装完成${NC}"
else
    echo -e "${GREEN}✓ Java已安装${NC}"
fi

# 安装Maven
echo -e "${YELLOW}[6/9] 安装Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    apt install -y maven
    echo -e "${GREEN}✓ Maven安装完成${NC}"
else
    echo -e "${GREEN}✓ Maven已安装${NC}"
fi

# 创建数据目录
echo -e "${YELLOW}[7/9] 创建数据目录...${NC}"
mkdir -p /opt/anime-platform/mysql
mkdir -p /opt/anime-platform/redis
mkdir -p /opt/anime-platform/nacos

# 生成 .env 文件
echo -e "${YELLOW}[8/9] 生成环境配置文件...${NC}"
cat > /root/anime-platform/.env << ENVEOF
# 动漫平台环境变量配置
# 由 setup-server.sh 自动生成

# ========== 数据库配置 ==========
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
MYSQL_USER=anime_user
MYSQL_PASSWORD=${MYSQL_PASSWORD}
MYSQL_DATABASE=anime_db
MYSQL_PORT=3306
MYSQL_HOST=127.0.0.1

# ========== Nacos配置 ==========
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_USERNAME=admin
NACOS_PASSWORD=${NACOS_PASSWORD}

# ========== JWT配置 ==========
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION=86400000

# ========== Redis配置 ==========
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD}

# ========== 服务端口配置 ==========
GATEWAY_PORT=8080
ANIME_SERVICE_PORT=8081
USER_SERVICE_PORT=8001
VIDEO_SERVICE_PORT=8002
DANMAKU_SERVICE_PORT=8003
CRAWLER_SERVICE_PORT=8085

# ========== Docker镜像标签 ==========
IMAGE_TAG=latest

# ========== 爬虫配置 ==========
CRAWLER_ENABLED=true
CRAWLER_CRON_JAPAN=0 0 */3 * * ?
CRAWLER_CRON_OTHER=0 0 */6 * * ?
ENVEOF

echo -e "${GREEN}✓ 环境配置文件已生成: /opt/anime-platform/.env${NC}"

# 复制 .env 到项目根目录
cp /opt/anime-platform/.env /opt/anime-platform/anime-platform/.env 2>/dev/null || true

echo -e "${YELLOW}[9/9] 准备启动基础设施服务...${NC}"

echo ""
echo -e "${CYAN}=========================================="
echo -e "${GREEN}基础设施服务配置信息"
echo -e "${CYAN}==========================================${NC}"
echo -e "${YELLOW}⚠  请妥善保存以下信息！${NC}"
echo ""
echo -e "${GREEN}MySQL 配置:${NC}"
echo -e "  主机: ${CYAN}127.0.0.1:3306${NC}"
echo -e "  Root密码: ${RED}${MYSQL_ROOT_PASSWORD}${NC}"
echo -e "  数据库: ${CYAN}anime_db${NC}"
echo -e "  用户: ${CYAN}anime_user${NC}"
echo -e "  密码: ${RED}${MYSQL_PASSWORD}${NC}"
echo ""
echo -e "${GREEN}Redis 配置:${NC}"
echo -e "  主机: ${CYAN}127.0.0.1:6379${NC}"
echo -e "  密码: ${RED}${REDIS_PASSWORD}${NC}"
echo ""
echo -e "${GREEN}Nacos 配置:${NC}"
echo -e "  地址: ${CYAN}127.0.0.1:8848${NC}"
echo -e "  用户名: ${CYAN}admin${NC}"
echo -e "  密码: ${RED}${NACOS_PASSWORD}${NC}"
echo ""
echo -e "${GREEN}JWT 密钥:${NC}"
echo -e "  Secret: ${RED}${JWT_SECRET}${NC}"
echo ""
echo -e "${CYAN}==========================================${NC}"
echo ""
echo -e "${GREEN}配置文件已保存到: /opt/anime-platform/.env${NC}"
echo ""
echo -e "${YELLOW}下一步:${NC}"
echo -e "  1. 复制配置文件到项目目录: cp /opt/anime-platform/.env /path/to/your/project/.env"
echo -e "  2. 启动所有服务: cd /path/to/your/project && docker compose up -d"
echo -e "  3. 查看服务日志: docker compose logs -f"
echo ""
