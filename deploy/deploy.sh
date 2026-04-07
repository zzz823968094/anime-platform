#!/bin/bash
# 动漫平台一键部署脚本（优化版 - 区分外部镜像与本地构建）
set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# 重试函数：最多重试 $1 次，间隔 $2 秒
retry() {
    local max_attempts=$1
    local delay=$2
    local cmd="${@:3}"
    local attempt=1

    until eval "$cmd"; do
        if (( attempt >= max_attempts )); then
            log_error "命令执行失败，已重试 ${max_attempts} 次: $cmd"
        fi
        log_warn "命令失败，${delay} 秒后重试 (${attempt}/${max_attempts}): $cmd"
        sleep "$delay"
        ((attempt++))
    done
}

echo "=== 动漫平台部署脚本（优化版）==="

# 1. 检查 Docker 环境
log_info "检查 Docker 环境..."
command -v docker &> /dev/null || log_error "Docker 未安装"
command -v docker compose &> /dev/null || log_error "docker compose 未安装"

# 2. 构建服务 JAR 包
log_info "检查并构建服务 JAR 包..."
SERVICES=("anime-service" "user-service" "video-service" "danmaku-service" "gateway")
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"  # 脚本位于 deploy/ 下，项目根目录为上一级

for service in "${SERVICES[@]}"; do
    service_dir="${PROJECT_ROOT}/${service}"
    target_dir="${service_dir}/target"
    
    # 检查是否已有 JAR 包
    if [ -d "$target_dir" ] && [ -n "$(find "$target_dir" -maxdepth 1 -name '*.jar' -print -quit)" ]; then
        log_info "$service JAR 包已存在，跳过构建"
    else
        log_info "构建 $service ..."
        (cd "$service_dir" && mvn clean package -DskipTests=true) || log_error "$service 构建失败"
        log_info "$service 构建成功"
    fi
done

# 3. 设置环境变量
export MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-123456}
export TAG=${TAG:-latest}
COMPOSE_FILE="docker-compose.prod.yml"

# 4. 停止并移除旧容器（可选）
log_info "停止并移除旧容器..."
docker compose -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null || true

# 5. 仅拉取外部中间件镜像（带重试，避免自定义镜像被错误拉取）
log_info "拉取外部中间件镜像（MySQL、Redis、Nacos）..."
MIDDLEWARE_IMAGES=("mysql:8.0" "redis:alpine" "nacos-registry.cn-hangzhou.cr.aliyuncs.com/nacos/nacos-server:v2.3.2")
for img in "${MIDDLEWARE_IMAGES[@]}"; do
    retry 3 5 "docker pull $img"
done

# 6. 构建自定义服务镜像（使用 docker compose build）
log_info "构建自定义服务镜像（anime-service, user-service 等）..."
docker compose -f "$COMPOSE_FILE" build --parallel

# 7. 启动所有服务
log_info "启动所有服务..."
if docker compose --help | grep -q -- "--wait"; then
    docker compose -f "$COMPOSE_FILE" up -d --wait --wait-timeout 60
else
    docker compose -f "$COMPOSE_FILE" up -d
    log_info "等待基础服务就绪..."
    # 等待 MySQL
    until docker compose -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -h"127.0.0.1" -P3306 -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent 2>/dev/null; do
        sleep 3
    done
    # 等待 Redis
    until docker compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping 2>/dev/null | grep -q PONG; do
        sleep 2
    done
    # 等待 Nacos
    until curl -s http://localhost:8848/nacos/ >/dev/null; do
        sleep 3
    done
    log_info "所有基础服务已就绪"
fi

# 8. 最终检查容器状态
log_info "当前容器状态："
docker compose -f "$COMPOSE_FILE" ps

log_info "=== 部署完成 ==="
echo "服务地址:"
echo "- 网关: http://localhost:8080"
echo "- Nacos控制台: http://localhost:8848/nacos/"
echo "- MySQL: localhost:3306 (root:${MYSQL_ROOT_PASSWORD})"
echo "- Redis: localhost:6379"