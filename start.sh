#!/bin/bash

# AnyDB Web 一键启动脚本
# 作者: AnyDB Team
# 版本: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    log_success "Docker和Docker Compose已安装"
}

# 检查端口占用
check_ports() {
    local ports=("80" "443" "3306" "5432" "6379" "8080" "9200" "27017")
    
    for port in "${ports[@]}"; do
        if netstat -tuln 2>/dev/null | grep -q ":$port "; then
            log_warning "端口 $port 已被占用"
        fi
    done
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    
    local dirs=("logs" "backups" "nginx/ssl" "mysql/conf.d")
    
    for dir in "${dirs[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log_success "创建目录: $dir"
        fi
    done
}

# 构建项目
build_project() {
    log_info "开始构建项目..."
    
    # 检查Java环境
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装Java 17+"
        exit 1
    fi
    
    # 构建后端
    log_info "构建后端项目..."
    cd backend
    if [ -f "pom.xml" ]; then
        mvn clean package -DskipTests -q || {
            log_error "后端构建失败"
            exit 1
        }
        log_success "后端构建完成"
    else
        log_warning "后端pom.xml文件不存在，跳过后端构建"
    fi
    cd ..
    
    # 检查Node.js环境
    if command -v npm &> /dev/null; then
        # 构建前端
        log_info "构建前端项目..."
        cd frontend
        if [ -f "package.json" ]; then
            npm install --silent || {
                log_error "前端依赖安装失败"
                exit 1
            }
            npm run build --silent || {
                log_error "前端构建失败"
                exit 1
            }
            log_success "前端构建完成"
        else
            log_warning "前端package.json文件不存在，跳过前端构建"
        fi
        cd ..
    else
        log_warning "Node.js未安装，跳过前端构建"
    fi
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    # 启动数据库服务
    log_info "启动数据库服务..."
    docker-compose up -d mysql postgresql redis elasticsearch mongodb
    
    # 等待数据库服务启动
    log_info "等待数据库服务启动..."
    sleep 30
    
    # 启动应用服务
    log_info "启动应用服务..."
    docker-compose up -d backend frontend nginx
    
    log_success "所有服务启动完成！"
}

# 显示服务状态
show_status() {
    log_info "检查服务状态..."
    
    if command -v docker-compose &> /dev/null; then
        docker-compose ps
    fi
    
    echo ""
    log_success "🚀 服务访问地址："
    echo -e "${GREEN}📱 前端界面: http://localhost${NC}"
    echo -e "${GREEN}🔌 后端API: http://localhost:8080/api${NC}"
    echo -e "${GREEN}📊 Druid监控: http://localhost:8080/api/druid${NC}"
    echo ""
    log_info "🔧 默认登录信息："
    echo -e "${YELLOW}用户名: admin${NC}"
    echo -e "${YELLOW}密码: admin${NC}"
    echo ""
}

# 停止服务
stop_services() {
    log_info "停止所有服务..."
    docker-compose down
    log_success "所有服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启所有服务..."
    docker-compose restart
    log_success "服务重启完成"
}

# 查看日志
show_logs() {
    if [ -n "$1" ]; then
        docker-compose logs -f "$1"
    else
        docker-compose logs -f
    fi
}

# 清理资源
cleanup() {
    log_warning "这将删除所有容器、数据和镜像，是否继续？ (y/N)"
    read -r response
    case "$response" in
        [yY][eE][sS]|[yY])
            log_info "开始清理..."
            docker-compose down -v --remove-orphans
            docker system prune -f
            log_success "清理完成"
            ;;
        *)
            log_info "取消清理操作"
            ;;
    esac
}

# 显示帮助信息
show_help() {
    echo "AnyDB Web 一键启动脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  start        启动所有服务"
    echo "  stop         停止所有服务"
    echo "  restart      重启所有服务"
    echo "  logs [service] 查看日志"
    echo "  build        构建项目"
    echo "  status       显示服务状态"
    echo "  cleanup      清理所有资源"
    echo "  help         显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start"
    echo "  $0 logs backend"
    echo "  $0 cleanup"
}

# 主函数
main() {
    echo -e "${BLUE}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║                        AnyDB Web                              ║"
    echo "║                  开源数据库管理平台                            ║"
    echo "║                                                               ║"
    echo "║  🌐 前端界面: http://localhost                                ║"
    echo "║  🔌 后端API: http://localhost:8080/api                       ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    
    case "${1:-start}" in
        "start")
            check_docker
            create_directories
            build_project
            start_services
            sleep 5
            show_status
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            restart_services
            show_status
            ;;
        "logs")
            show_logs "$2"
            ;;
        "build")
            build_project
            ;;
        "status")
            show_status
            ;;
        "cleanup")
            cleanup
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口点
main "$@"