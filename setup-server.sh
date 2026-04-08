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

# 更新系统
echo -e "${YELLOW}[1/6] 更新系统包...${NC}"
apt update && apt upgrade -y

# 安装基础工具
echo -e "${YELLOW}[2/6] 安装基础工具...${NC}"
apt install -y git curl wget unzip net-tools vim

# 安装Docker
echo -e "${YELLOW}[3/6] 安装Docker...${NC}"
if ! command -v docker &> /dev/null; then
    apt install -y docker.io
    systemctl start docker
    systemctl enable docker
    echo -e "${GREEN}✓ Docker安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker已安装${NC}"
fi

# 安装Docker Compose
echo -e "${YELLOW}[4/6] 安装Docker Compose...${NC}"
if ! command -v docker-compose &> /dev/null; then
    apt install -y docker-compose
    echo -e "${GREEN}✓ Docker Compose安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker Compose已安装${NC}"
fi

# 安装Java 17
echo -e "${YELLOW}[5/6] 安装Java 17...${NC}"
if ! command -v java &> /dev/null; then
    apt install -y openjdk-17-jdk
    echo -e "${GREEN}✓ Java安装完成${NC}"
else
    echo -e "${GREEN}✓ Java已安装${NC}"
fi

# 安装Maven
echo -e "${YELLOW}[6/6] 安装Maven...${NC}"
if ! command -v mvn &> /dev/null; then
    apt install -y maven
    echo -e "${GREEN}✓ Maven安装完成${NC}"
else
    echo -e "${GREEN}✓ Maven已安装${NC}"
fi

# 验证安装
echo ""
echo -e "${BLUE}=========================================="
echo -e "${GREEN}环境初始化完成！"
echo -e "${BLUE}==========================================${NC}"
echo ""
echo "已安装组件:"
echo "  Docker:        $(docker --version)"
echo "  Docker Compose: $(docker-compose --version)"
echo "  Java:          $(java -version 2>&1 | head -n 1)"
echo "  Maven:         $(mvn -version 2>&1 | head -n 1)"
echo "  Git:           $(git --version)"
echo ""
echo "下一步:"
echo "  1. 运行部署脚本: sudo bash deploy.sh"
echo "  2. 或查看部署文档: cat DEPLOY_GUIDE.md"
echo ""
