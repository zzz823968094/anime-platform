#!/bin/bash

################################################################################
# 动漫平台 - 阿里云Ubuntu快速部署脚本
# 用途: 从Git仓库拉取最新代码，构建并部署到Docker容器
# 适用系统: Ubuntu 20.04/22.04/24.04
################################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
GIT_REPO="https://github.com/zzz823968094/anime-platform.git"  # 替换为你的Git仓库地址
PROJECT_DIR="/root/anime-platform"
LOG_FILE="/root/log/anime-platform-deploy.log"
BACKUP_DIR="/root/anime-platform-backup"
ENV_FILE="/root/.env.example"  # .env.example 文件路径（服务器 root 目录）

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
    echo -e "${GREEN}"
    echo "=========================================="
    echo "   动漫平台 - 自动化部署脚本"
    echo "   Anime Platform Auto Deploy Script"
    echo "=========================================="
    echo -e "${NC}"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        error "请使用root用户或sudo运行此脚本"
        echo "示例: sudo bash deploy.sh"
        exit 1
    fi
}

# 检查系统要求
check_system() {
    log "检查系统要求..."

    # 检查操作系统
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
        log "检测到系统: $OS $VER"
    else
        error "无法检测操作系统版本"
        exit 1
    fi

    # 检查架构
    ARCH=$(uname -m)
    log "系统架构: $ARCH"
}

# 安装依赖
install_dependencies() {
    log "开始安装依赖..."

    # 更新包列表
    apt-get update -y

    # 安装必要工具
    apt-get install -y \
        git \
        curl \
        wget \
        unzip \
        openjdk-17-jdk \
        maven \
        docker.io \
        docker compose \
        net-tools

    # 启动并启用Docker
    systemctl start docker
    systemctl enable docker

    success "依赖安装完成"
}

# 检查Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker未安装，正在安装..."
        install_dependencies
    fi

    if ! command -v docker compose &> /dev/null; then
        error "Docker Compose未安装，正在安装..."
        install_dependencies
    fi

    success "Docker版本: $(docker --version)"
    success "Docker Compose版本: $(docker compose --version)"
}

# 检查Java和Maven
check_java_maven() {
    if ! command -v java &> /dev/null; then
        error "Java未安装，正在安装..."
        install_dependencies
    fi

    if ! command -v mvn &> /dev/null; then
        error "Maven未安装，正在安装..."
        install_dependencies
    fi

    success "Java版本: $(java -version 2>&1 | head -n 1)"
    success "Maven版本: $(mvn -version 2>&1 | head -n 1)"
}

# 从Git拉取代码
pull_code() {
    log "开始从Git拉取代码..."

    if [ -d "$PROJECT_DIR/.git" ]; then
        # 已存在，执行pull
        log "项目目录已存在，执行git pull..."
        cd "$PROJECT_DIR"
        git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || {
            error "git pull失败，尝试重新克隆..."
            cd /tmp
            rm -rf "$PROJECT_DIR"
            git clone "$GIT_REPO" "$PROJECT_DIR"
        }
    else
        # 首次克隆
        log "首次克隆代码仓库..."
        mkdir -p "$(dirname $PROJECT_DIR)"
        git clone "$GIT_REPO" "$PROJECT_DIR"
    fi

    cd "$PROJECT_DIR"
    success "代码拉取完成"
    log "当前分支: $(git branch --show-current)"
    log "最新提交: $(git log -1 --pretty=format:'%h %s')"
}

# 配置环境变量文件
setup_env_file() {
    log "配置环境变量文件..."
    cd "$PROJECT_DIR"

    if [ -f ".env" ]; then
        log ".env 文件已存在，跳过配置"
    else
        log ".env 文件不存在，开始创建..."
        
        # 优先从服务器 root 目录复制
        if [ -f "$ENV_FILE" ]; then
            log "从 $ENV_FILE 复制 .env 文件..."
            cp "$ENV_FILE" .env
            success ".env 文件已创建（来源: $ENV_FILE）"
        elif [ -f ".env.example" ]; then
            log "从项目 .env.example 复制 .env 文件..."
            cp .env.example .env
            success ".env 文件已创建（来源: 项目 .env.example）"
        else
            error "未找到 .env.example 文件"
            error "请确保以下任一文件存在："
            error "  1. $ENV_FILE"
            error "  2. $PROJECT_DIR/.env.example"
            exit 1
        fi

        log "配置文件将在启动前自动检查必需的环境变量"
    fi
}

# 验证环境变量
validate_env() {
    log "验证环境变量..."
    cd "$PROJECT_DIR"

    if [ ! -f ".env" ]; then
        error ".env 文件不存在"
        exit 1
    fi

    # 检查关键环境变量
    local missing_vars=()
    
    while IFS='=' read -r key value; do
        # 跳过注释和空行
        [[ "$key" =~ ^#.*$ ]] && continue
        [[ -z "$key" ]] && continue
        
        # 检查必需的非空值
        case "$key" in
            MYSQL_ROOT_PASSWORD|MYSQL_PASSWORD|REDIS_PASSWORD|JWT_SECRET)
                if [[ "$value" == *"your_"* ]] || [[ "$value" == *"change_this"* ]] || [ -z "$value" ]; then
                    missing_vars+=("$key")
                fi
                ;;
        esac
    done < .env

    if [ ${#missing_vars[@]} -gt 0 ]; then
        warning "以下环境变量使用了默认值或为空，建议修改："
        for var in "${missing_vars[@]}"; do
            warning "  - $var"
        done
        echo ""
        warning "编辑命令: nano .env"
        echo ""
        
        # 询问是否继续
        read -p "是否继续部署？(y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            error "部署已取消"
            exit 1
        fi
    else
        success "环境变量验证通过"
    fi
}

# 备份当前运行版本
backup_current() {
    if [ -d "$PROJECT_DIR" ]; then
        log "备份当前版本..."
        mkdir -p "$BACKUP_DIR"
        BACKUP_NAME="anime-platform-$(date +%Y%m%d_%H%M%S)"
        cp -r "$PROJECT_DIR" "$BACKUP_DIR/$BACKUP_NAME"
        success "备份完成: $BACKUP_DIR/$BACKUP_NAME"
    fi
}

# 停止并清理旧容器
stop_containers() {
    log "停止旧容器..."
    cd "$PROJECT_DIR"

    if [ -f docker-compose.yml ]; then
        docker compose down 2>/dev/null || true
        success "旧容器已停止"
    else
        warning "未找到docker-compose.yml"
    fi
}

# 构建Java项目
build_project() {
    log "开始构建项目..."
    cd "$PROJECT_DIR"

    # 清理旧构建
    log "清理旧的构建文件..."
    mvn clean 2>/dev/null || true

    # 编译打包（跳过测试以加快速度）
    log "使用Maven编译打包（跳过测试）..."
    mvn package -DskipTests -B

    if [ $? -eq 0 ]; then
        success "项目构建成功"
    else
        error "项目构建失败，请检查错误日志"
        exit 1
    fi
}

# 构建Docker镜像
build_docker_images() {
    log "开始构建Docker镜像..."
    cd "$PROJECT_DIR"

    # 构建所有服务的Docker镜像
    docker compose build

    if [ $? -eq 0 ]; then
        success "Docker镜像构建完成"
    else
        error "Docker镜像构建失败"
        exit 1
    fi
}

# 启动服务
start_services() {
    log "启动所有服务..."
    cd "$PROJECT_DIR"

    # 使用docker compose启动
    docker compose up -d

    if [ $? -eq 0 ]; then
        success "所有服务已启动"
    else
        error "服务启动失败"
        exit 1
    fi

    # 等待服务启动
    log "等待服务启动（30秒）..."
    sleep 30
}

# 检查服务状态
check_services() {
    log "检查服务运行状态..."
    cd "$PROJECT_DIR"

    echo ""
    echo -e "${BLUE}=========================================="
    echo "   服务运行状态"
    echo "==========================================${NC}"

    docker compose ps

    echo ""
    log "检查各服务端口..."

    # 检查关键端口
    declare -A PORTS
    PORTS=(
        ["Gateway"]="8080"
        ["Anime Service"]="8081"
        ["User Service"]="8001"
        ["Video Service"]="8002"
        ["Danmaku Service"]="8003"
        ["MySQL"]="3306"
        ["Redis"]="6379"
        ["Nacos"]="8848"
    )

    for service in "${!PORTS[@]}"; do
        port=${PORTS[$service]}
        if netstat -tuln 2>/dev/null | grep -q ":$port " || ss -tuln 2>/dev/null | grep -q ":$port "; then
            success "$service (端口: $port) - 运行中"
        else
            warning "$service (端口: $port) - 未检测到"
        fi
    done

    echo ""
}

# 显示部署信息
show_deploy_info() {
    echo -e "${GREEN}"
    echo "=========================================="
    echo "   部署完成！"
    echo "=========================================="
    echo -e "${NC}"

    echo "项目目录: $PROJECT_DIR"
    echo "日志文件: $LOG_FILE"
    echo "备份目录: $BACKUP_DIR"
    echo ""

    echo -e "${YELLOW}服务访问地址:${NC}"
    echo "  API网关:        http://$(curl -s ifconfig.me 2>/dev/null || echo '服务器IP'):8080"
    echo "  Nacos控制台:    http://$(curl -s ifconfig.me 2>/dev/null || echo '服务器IP'):8848/nacos"
    echo ""

    echo -e "${YELLOW}常用命令:${NC}"
    echo "  查看服务状态:   cd $PROJECT_DIR && docker compose ps"
    echo "  查看服务日志:   cd $PROJECT_DIR && docker compose logs -f [服务名]"
    echo "  停止所有服务:   cd $PROJECT_DIR && docker compose down"
    echo "  重启服务:       cd $PROJECT_DIR && docker compose restart [服务名]"
    echo "  重新部署:       sudo bash deploy.sh"
    echo "  编辑环境变量:   nano .env"
    echo ""

    echo -e "${YELLOW}Docker容器列表:${NC}"
    echo "  anime-gateway         - API网关 (8080)"
    echo "  anime-service         - 动漫服务 (8081)"
    echo "  user-service          - 用户服务 (8001)"
    echo "  video-service         - 视频服务 (8002)"
    echo "  danmaku-service       - 弹幕服务 (8003)"
    echo "  anime-mysql           - MySQL数据库 (3306)"
    echo "  anime-redis           - Redis缓存 (6379)"
    echo "  anime-nacos           - Nacos注册中心 (8848)"
    echo ""
}

# 主函数
main() {
    print_banner

    log "========== 开始部署 =========="

    # 1. 检查root权限
    check_root

    # 2. 检查系统
    check_system

    # 3. 检查依赖
    check_docker
    check_java_maven

    # 4. 拉取代码
    pull_code

    # 5. 配置环境变量文件
    setup_env_file

    # 6. 验证环境变量
    validate_env

    # 7. 停止旧服务
    stop_containers

    # 8. 构建项目
    build_project

    # 9. 构建Docker镜像
    build_docker_images

    # 10. 启动服务
    start_services

    # 11. 检查服务状态
    check_services

    # 12. 显示部署信息
    show_deploy_info

    log "========== 部署完成 =========="

    success "部署成功！如有问题，请查看日志: tail -f $LOG_FILE"
}

# 执行主函数
main "$@"
