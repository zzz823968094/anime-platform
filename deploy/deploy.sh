#!/bin/bash

# 部署脚本
set -e

echo "=== 动漫平台部署脚本 ==="

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误：Docker未安装，请先安装Docker"
    exit 1
fi

# 检查docker compose是否安装
if ! command -v docker compose &> /dev/null; then
    echo "错误：docker compose未安装，请先安装docker compose"
    exit 1
fi

# 构建服务（如果需要）
echo "步骤1: 检查并构建服务JAR包..."
mkdir -p ../common/target
mkdir -p ../anime-service/target
mkdir -p ../user-service/target
mkdir -p ../video-service/target
mkdir -p ../danmaku-service/target
mkdir -p ../gateway/target

for service in anime-service user-service video-service danmaku-service gateway; do
    if [ ! -f "../$service/target/*.jar" ]; then
        echo "构建 $service ..."
        cd ../$service
        mvn clean package -DskipTests=true
        if [ $? -eq 0 ]; then
            echo "$service 构建成功"
        else
            echo "$service 构建失败，请检查错误信息"
            exit 1
        fi
    else
        echo "$service JAR包已存在，跳过构建"
    fi
done


# 设置环境变量
export MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-anime123}
export TAG=${TAG:-latest}

# 停止并删除现有容器
echo "步骤2: 清理现有容器..."
docker compose -f docker-compose.prod.yml down --remove-orphans

# 启动所有服务
echo "步骤3: 启动所有服务..."
docker compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo "步骤4: 等待服务启动..."
sleep 30

# 检查容器状态
echo "步骤5: 检查容器状态..."
docker compose -f docker-compose.prod.yml ps

echo "=== 部署完成 ==="
echo "服务地址:"
echo "- 网关: http://localhost:8080"
echo "- Nacos控制台: http://localhost:8848/nacos/"
echo "- MySQL: localhost:3306 (root:${MYSQL_ROOT_PASSWORD})"
echo "- Redis: localhost:6379"
