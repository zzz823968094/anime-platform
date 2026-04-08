#!/bin/bash

################################################################################
# 动漫平台 - 简化版部署脚本
# 用途: 快速更新部署，适合日常使用
################################################################################

set -e

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_DIR="/opt/anime-platform"

echo -e "${BLUE}================================${NC}"
echo -e "${GREEN}动漫平台 - 快速更新部署${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

# 检查root权限
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}✗ 请使用sudo运行此脚本${NC}"
    exit 1
fi

# 进入项目目录
cd "$PROJECT_DIR" || {
    echo -e "${RED}✗ 项目目录不存在: $PROJECT_DIR${NC}"
    exit 1
}

echo -e "${YELLOW}1. 拉取最新代码...${NC}"
git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || {
    echo -e "${RED}✗ Git pull失败${NC}"
    exit 1
}

echo -e "${YELLOW}2. 停止旧服务...${NC}"
docker-compose down

echo -e "${YELLOW}3. 编译项目...${NC}"
mvn clean package -DskipTests -B

echo -e "${YELLOW}4. 构建Docker镜像...${NC}"
docker-compose build

echo -e "${YELLOW}5. 启动服务...${NC}"
docker-compose up -d

echo ""
echo -e "${YELLOW}等待服务启动 (30秒)...${NC}"
sleep 30

echo ""
echo -e "${YELLOW}6. 检查服务状态...${NC}"
docker-compose ps

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}✓ 部署完成！${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "访问地址:"
echo "  API网关:     http://$(curl -s ifconfig.me 2>/dev/null || echo '服务器IP'):8080"
echo "  Nacos控制台: http://$(curl -s ifconfig.me 2>/dev/null || echo '服务器IP'):8848/nacos"
echo ""
echo "常用命令:"
echo "  查看日志: cd $PROJECT_DIR && docker-compose logs -f [服务名]"
echo "  重启服务: cd $PROJECT_DIR && docker-compose restart [服务名]"
echo ""
