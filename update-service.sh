#!/bin/bash

################################################################################
# 动漫平台 - 单服务更新脚本
# 用途: 单独更新指定的微服务，无需重新部署所有服务
# 适用系统: Ubuntu 20.04/22.04/24.04
# 使用方法: 
#   sudo bash update-service.sh [服务名]
#   示例: sudo bash update-service.sh gateway
#         sudo bash update-service.sh admin-service
#         sudo bash update-service.sh gateway admin-service user-service
################################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置变量
PROJECT_DIR="/root/anime-platform"
LOG_FILE="/root/log/anime-platform-update.log"
GIT_REPO="https://github.com/zzz823968094/anime-platform.git"

# 服务列表及其对应信息
declare -A SERVICE_INFO
SERVICE_INFO=(
    ["gateway"]="API网关|8080|./gateway"
    ["admin-service"]="后台管理服务|8086|./admin-service"
    ["anime-service"]="动漫服务|8082|./anime-service"
    ["user-service"]="用户服务|8081|./user-service"
    ["video-service"]="视频服务|8083|./video-service"
    ["danmaku-service"]="弹幕服务|8084|./danmaku-service"
    ["crawler-service"]="爬虫服务|8085|./crawler-service"
)

# 日志函数
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

success() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] ✓ $1${NC}" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] ⚠ $1${NC}" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ✗ $1${NC}" | tee -a "$LOG_FILE"
}

# 打印横幅
print_banner() {
    echo -e "${CYAN}"
    echo "=========================================="
    echo "   动漫平台 - 单服务更新脚本"
    echo "   Anime Platform Service Update"
    echo "=========================================="
    echo -e "${NC}"
}

# 检查root权限
check_root() {
    if [ "$EUID" -ne 0 ]; then
        error "请使用root用户或sudo运行此脚本"
        echo "示例: sudo bash update-service.sh [服务名]"
        exit 1
    fi
}

# 检查Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker未安装"
        exit 1
    fi

    if ! command -v docker compose &> /dev/null; then
        error "Docker Compose未安装"
        exit 1
    fi

    success "Docker版本: $(docker --version)"
    success "Docker Compose版本: $(docker compose --version)"
}

# 显示可用服务列表
show_available_services() {
    echo -e "${YELLOW}可用服务列表:${NC}"
    echo ""
    printf "%-20s %-20s %-10s\n" "服务名" "描述" "端口"
    echo "--------------------------------------------------------------"
    for service in "${!SERVICE_INFO[@]}"; do
        IFS='|' read -r desc port path <<< "${SERVICE_INFO[$service]}"
        printf "%-20s %-20s %-10s\n" "$service" "$desc" "$port"
    done
    echo ""
}

# 从Git拉取最新代码
pull_code() {
    log "开始从Git拉取代码..."

    if [ -d "$PROJECT_DIR/.git" ]; then
        cd "$PROJECT_DIR"
        git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || {
            error "git pull失败"
            exit 1
        }
    else
        error "项目目录不存在: $PROJECT_DIR"
        exit 1
    fi

    success "代码拉取完成"
    log "最新提交: $(git log -1 --pretty=format:'%h %s')"
}

# 验证服务名
validate_services() {
    local services=("$@")
    local valid_services=()

    for service in "${services[@]}"; do
        if [[ -n "${SERVICE_INFO[$service]}" ]]; then
            valid_services+=("$service")
        else
            error "无效的服务名: $service"
        fi
    done

    if [ ${#valid_services[@]} -eq 0 ]; then
        error "没有有效的服务名"
        show_available_services
        exit 1
    fi

    echo "${valid_services[@]}"
}

# 构建指定服务
build_service() {
    local service=$1
    cd "$PROJECT_DIR"

    log "=========================================="
    log "开始构建服务: $service"
    log "=========================================="

    # 获取服务信息
    IFS='|' read -r desc port path <<< "${SERVICE_INFO[$service]}"

    # 使用Maven构建Java项目（构建整个项目以确保依赖正确）
    log "使用Maven构建$service..."
    mvn package -DskipTests -B -pl "$path" -am

    if [ $? -eq 0 ]; then
        success "$service Java构建成功"
    else
        error "$service Java构建失败"
        return 1
    fi

    # 构建Docker镜像
    log "构建$service Docker镜像..."
    docker compose build "$service"

    if [ $? -eq 0 ]; then
        success "$service Docker镜像构建完成"
    else
        error "$service Docker镜像构建失败"
        return 1
    fi

    return 0
}

# 停止指定服务
stop_service() {
    local service=$1
    cd "$PROJECT_DIR"

    log "停止服务: $service"
    docker compose stop "$service"

    if [ $? -eq 0 ]; then
        success "$service 已停止"
    else
        error "$service 停止失败"
        return 1
    fi

    return 0
}

# 启动指定服务
start_service() {
    local service=$1
    cd "$PROJECT_DIR"

    log "启动服务: $service"
    docker compose up -d "$service"

    if [ $? -eq 0 ]; then
        success "$service 已启动"
    else
        error "$service 启动失败"
        return 1
    fi

    return 0
}

# 重启指定服务
restart_service() {
    local service=$1
    cd "$PROJECT_DIR"

    log "重启服务: $service"
    docker compose restart "$service"

    if [ $? -eq 0 ]; then
        success "$service 已重启"
    else
        error "$service 重启失败"
        return 1
    fi

    return 0
}

# 检查服务状态
check_service_status() {
    local service=$1
    cd "$PROJECT_DIR"

    log "检查$service状态..."
    
    # 获取服务信息
    IFS='|' read -r desc port path <<< "${SERVICE_INFO[$service]}"
    
    # 检查容器状态
    local status=$(docker compose ps "$service" --format "json" 2>/dev/null | grep -o '"State":"[^"]*"' | head -1 | cut -d'"' -f4)
    
    if [[ "$status" == "running" ]]; then
        success "$service ($desc) - 运行中 (端口: $port)"
    else
        warning "$service ($desc) - 状态: $status (端口: $port)"
    fi
}

# 查看服务日志
view_service_logs() {
    local service=$1
    local lines=${2:-100}
    cd "$PROJECT_DIR"

    log "查看$service最近$log行日志..."
    echo -e "${CYAN}========== $service 日志 ==========${NC}"
    docker compose logs --tail="$lines" "$service"
    echo -e "${CYAN}=================================${NC}"
}

# 更新单个服务
update_single_service() {
    local service=$1
    local action=${2:-"restart"}  # restart 或 update

    echo ""
    echo -e "${BLUE}=========================================="
    echo "   更新服务: $service"
    echo "==========================================${NC}"

    # 构建服务
    if ! build_service "$service"; then
        error "$service 构建失败，跳过更新"
        return 1
    fi

    # 根据操作类型执行
    case "$action" in
        "restart")
            if ! restart_service "$service"; then
                error "$service 重启失败"
                return 1
            fi
            ;;
        "update")
            if ! stop_service "$service"; then
                error "$service 停止失败"
                return 1
            fi
            
            sleep 2
            
            if ! start_service "$service"; then
                error "$service 启动失败"
                return 1
            fi
            ;;
    esac

    # 等待服务启动
    log "等待$service启动（10秒）..."
    sleep 10

    # 检查服务状态
    check_service_status "$service"

    success "$service 更新完成！"
    return 0
}

# 批量更新服务
update_multiple_services() {
    local services=("$@")
    local failed_services=()

    echo ""
    echo -e "${CYAN}=========================================="
    echo "   批量更新服务"
    echo "   服务列表: ${services[*]}"
    echo "==========================================${NC}"

    for service in "${services[@]}"; do
        if ! update_single_service "$service" "update"; then
            failed_services+=("$service")
        fi
        echo ""
    done

    # 显示结果
    echo ""
    echo -e "${BLUE}=========================================="
    echo "   批量更新完成"
    echo "==========================================${NC}"
    
    if [ ${#failed_services[@]} -gt 0 ]; then
        warning "以下服务更新失败:"
        for failed in "${failed_services[@]}"; do
            warning "  - $failed"
        done
        return 1
    else
        success "所有服务更新成功！"
        return 0
    fi
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}使用方法:${NC}"
    echo "  sudo bash update-service.sh [选项] [服务名...]"
    echo ""
    echo -e "${CYAN}选项:${NC}"
    echo "  -h, --help          显示帮助信息"
    echo "  -l, --list          显示可用服务列表"
    echo "  -s, --status        查看所有服务状态"
    echo "  -a, --all           更新所有服务"
    echo "  --logs [服务名]     查看服务日志（最近100行）"
    echo "  --logs [服务名] [n] 查看服务日志（最近n行）"
    echo ""
    echo -e "${CYAN}示例:${NC}"
    echo "  # 更新单个服务"
    echo "  sudo bash update-service.sh gateway"
    echo "  sudo bash update-service.sh admin-service"
    echo ""
    echo "  # 更新多个服务"
    echo "  sudo bash update-service.sh gateway admin-service user-service"
    echo ""
    echo "  # 更新所有服务"
    echo "  sudo bash update-service.sh --all"
    echo ""
    echo "  # 查看服务状态"
    echo "  sudo bash update-service.sh --status"
    echo ""
    echo "  # 查看服务日志"
    echo "  sudo bash update-service.sh --logs gateway"
    echo "  sudo bash update-service.sh --logs gateway 200"
    echo ""
    echo -e "${CYAN}可用服务:${NC}"
    show_available_services
}

# 主函数
main() {
    print_banner

    # 检查参数
    if [ $# -eq 0 ]; then
        warning "未指定服务名"
        show_help
        exit 1
    fi

    # 检查root权限
    check_root

    # 检查Docker
    check_docker

    # 处理特殊选项
    case "$1" in
        -h|--help)
            show_help
            exit 0
            ;;
        -l|--list)
            show_available_services
            exit 0
            ;;
        -s|--status)
            log "检查所有服务状态..."
            for service in "${!SERVICE_INFO[@]}"; do
                check_service_status "$service"
            done
            exit 0
            ;;
        -a|--all)
            log "准备更新所有服务..."
            pull_code
            
            local all_services=("${!SERVICE_INFO[@]}")
            update_multiple_services "${all_services[@]}"
            exit $?
            ;;
        --logs)
            if [ -z "$2" ]; then
                error "请指定服务名"
                show_available_services
                exit 1
            fi
            
            if [[ -z "${SERVICE_INFO[$2]}" ]]; then
                error "无效的服务名: $2"
                show_available_services
                exit 1
            fi
            
            view_service_logs "$2" "${3:-100}"
            exit 0
            ;;
    esac

    # 拉取最新代码
    pull_code

    # 验证并更新服务
    local services_to_update=()
    for arg in "$@"; do
        if [[ -n "${SERVICE_INFO[$arg]}" ]]; then
            services_to_update+=("$arg")
        else
            error "无效的服务名: $arg"
        fi
    done

    if [ ${#services_to_update[@]} -eq 0 ]; then
        error "没有有效的服务名"
        show_available_services
        exit 1
    fi

    if [ ${#services_to_update[@]} -eq 1 ]; then
        # 单个服务更新
        update_single_service "${services_to_update[0]}" "update"
    else
        # 多个服务更新
        update_multiple_services "${services_to_update[@]}"
    fi

    exit $?
}

# 执行主函数
main "$@"
