#!/bin/bash

# AstraDB Module Deployment Script
# AstraDB模块部署脚本

# Enable strict error handling - exit on any command failure
# 启用严格错误处理 - 任何命令失败时退出
set -e
set -o pipefail

# Color codes for output
# 输出颜色代码
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
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

# Check if command exists
# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Main execution
# 主执行流程
main() {
    log_info "=== AstraDB Module Deployment ==="
    log_info "=== AstraDB模块部署 ==="
    
    # Check prerequisites
    # 检查前置条件
    if ! command_exists mvn; then
        log_error "Maven is not installed. Please install Maven first."
        log_error "Maven未安装。请先安装Maven。"
        exit 1
    fi
    
    local deploy_target=${DEPLOY_TARGET:-"local"}
    local skip_tests=${SKIP_TESTS:-true}
    
    log_info "Deployment target: $deploy_target"
    log_info "部署目标: $deploy_target"
    
    # Ensure module is built first
    # 确保模块首先被构建
    log_info "Building module before deployment..."
    log_info "部署前构建模块..."
    
    local build_cmd="clean package"
    if [ "$skip_tests" = "true" ]; then
        build_cmd="$build_cmd -DskipTests"
        log_info "Skipping tests during build"
        log_info "构建期间跳过测试"
    fi
    
    if mvn $build_cmd; then
        log_success "Module built successfully"
        log_success "模块构建成功"
    else
        log_error "Failed to build module"
        log_error "模块构建失败"
        exit 1
    fi
    
    # Deploy based on target
    # 根据目标进行部署
    case $deploy_target in
        "local")
            deploy_local
            ;;
        "remote")
            deploy_remote
            ;;
        "snapshot")
            deploy_snapshot
            ;;
        *)
            log_error "Unknown deployment target: $deploy_target"
            log_error "未知的部署目标: $deploy_target"
            exit 1
            ;;
    esac
    
    log_success "=== AstraDB module deployment completed! ==="
    log_success "=== AstraDB模块部署完成！ ==="
}

# Deploy to local repository
# 部署到本地仓库
deploy_local() {
    log_info "Deploying to local Maven repository..."
    log_info "部署到本地Maven仓库..."
    
    if mvn install; then
        log_success "Successfully deployed to local repository"
        log_success "成功部署到本地仓库"
        
        # Show local repository location
        # 显示本地仓库位置
        local local_repo=$(mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout 2>/dev/null || echo "~/.m2/repository")
        log_info "Local repository: $local_repo"
        log_info "本地仓库: $local_repo"
    else
        log_error "Failed to deploy to local repository"
        log_error "部署到本地仓库失败"
        exit 1
    fi
}

# Deploy to remote repository
# 部署到远程仓库
deploy_remote() {
    log_info "Deploying to remote repository..."
    log_info "部署到远程仓库..."
    
    # Check if distribution management is configured
    # 检查是否配置了分发管理
    if mvn help:effective-pom | grep -q "<distributionManagement>"; then
        if mvn deploy; then
            log_success "Successfully deployed to remote repository"
            log_success "成功部署到远程仓库"
        else
            log_error "Failed to deploy to remote repository"
            log_error "部署到远程仓库失败"
            exit 1
        fi
    else
        log_error "No distribution management configuration found"
        log_error "未找到分发管理配置"
        log_info "Please configure <distributionManagement> in pom.xml"
        log_info "请在pom.xml中配置<distributionManagement>"
        exit 1
    fi
}

# Deploy snapshot version
# 部署快照版本
deploy_snapshot() {
    log_info "Deploying snapshot version..."
    log_info "部署快照版本..."
    
    # Check if version is snapshot
    # 检查版本是否为快照
    local version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)
    if [[ $version == *"-SNAPSHOT" ]]; then
        if mvn deploy; then
            log_success "Successfully deployed snapshot version: $version"
            log_success "成功部署快照版本: $version"
        else
            log_error "Failed to deploy snapshot version"
            log_error "快照版本部署失败"
            exit 1
        fi
    else
        log_error "Current version is not a snapshot: $version"
        log_error "当前版本不是快照版本: $version"
        exit 1
    fi
}

# Handle command line arguments
# 处理命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --local)
            DEPLOY_TARGET="local"
            shift
            ;;
        --remote)
            DEPLOY_TARGET="remote"
            shift
            ;;
        --snapshot)
            DEPLOY_TARGET="snapshot"
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --with-tests)
            SKIP_TESTS=false
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  --local           Deploy to local Maven repository (default)"
            echo "  --remote          Deploy to remote repository"
            echo "  --snapshot        Deploy snapshot version to repository"
            echo "  --skip-tests      Skip tests during build (default)"
            echo "  --with-tests      Run tests during build"
            echo "  -h, --help        Show this help message"
            echo ""
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --local           部署到本地Maven仓库（默认）"
            echo "  --remote          部署到远程仓库"
            echo "  --snapshot        部署快照版本到仓库"
            echo "  --skip-tests      构建期间跳过测试（默认）"
            echo "  --with-tests      构建期间运行测试"
            echo "  -h, --help        显示此帮助信息"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            log_error "未知选项: $1"
            echo "Use --help for usage information / 使用 --help 查看使用信息"
            exit 1
            ;;
    esac
done

# Run main function
# 运行主函数
main "$@"
