#!/bin/bash

# USearch Module Deployment Script
# USearch模块部署脚本

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
    log_info "=== USearch Module Deployment ==="
    log_info "=== USearch模块部署 ==="
    
    # Check prerequisites
    # 检查前置条件
    if ! command_exists mvn; then
        log_error "Maven is not installed. Please install Maven first."
        log_error "Maven未安装。请先安装Maven。"
        exit 1
    fi
    
    # Check if USearch JAR exists
    # 检查USearch JAR是否存在
    local usearch_jar="lib/usearch-2.21.0.jar"
    if [ ! -f "$usearch_jar" ]; then
        log_warning "USearch JAR not found at: $usearch_jar"
        log_warning "USearch JAR未找到：$usearch_jar"
        log_info "Running dependency installation first..."
        log_info "先运行依赖安装..."
        
        if [ -f "dependency.sh" ]; then
            bash dependency.sh
        else
            log_error "dependency.sh not found. Please install dependencies first."
            log_error "dependency.sh未找到。请先安装依赖。"
            exit 1
        fi
    fi
    
    log_info "Starting deployment process..."
    log_info "开始部署过程..."
    
    # Run tests before deployment (optional, can be skipped with -DskipTests)
    # 部署前运行测试（可选，可以用-DskipTests跳过）
    local skip_tests=${SKIP_TESTS:-true}
    
    if [ "$skip_tests" = "false" ]; then
        log_info "Running tests before deployment..."
        log_info "部署前运行测试..."
        if ! mvn test; then
            log_error "Tests failed. Deployment aborted."
            log_error "测试失败。部署中止。"
            exit 1
        fi
        log_success "Tests passed successfully"
        log_success "测试通过"
    else
        log_info "Skipping tests for faster deployment"
        log_info "跳过测试以加快部署速度"
    fi
    
    # Deploy to Maven repository
    # 部署到Maven仓库
    log_info "Deploying to Maven repository..."
    log_info "部署到Maven仓库..."
    
    local deploy_args="clean deploy"
    if [ "$skip_tests" = "true" ]; then
        deploy_args="$deploy_args -DskipTests"
    fi
    
    if mvn $deploy_args; then
        log_success "Deployment completed successfully"
        log_success "部署成功完成"
    else
        log_error "Deployment failed"
        log_error "部署失败"
        exit 1
    fi
    
    log_success "=== USearch module deployment completed! ==="
    log_success "=== USearch模块部署完成！ ==="
    
    # Show deployment summary
    # 显示部署摘要
    log_info "Deployment Summary / 部署摘要:"
    log_info "  - Module: ali-langengine-usearch"
    log_info "  - Status: Successfully deployed"
    log_info "  - Tests: $([ "$skip_tests" = "true" ] && echo "Skipped" || echo "Passed")"
}

# Handle command line arguments
# 处理命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --with-tests)
            SKIP_TESTS=false
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--with-tests|--skip-tests]"
            echo "  --with-tests    Run tests before deployment"
            echo "  --skip-tests    Skip tests during deployment (default)"
            echo "用法: $0 [--with-tests|--skip-tests]"
            echo "  --with-tests    部署前运行测试"
            echo "  --skip-tests    部署时跳过测试（默认）"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            log_error "未知选项: $1"
            exit 1
            ;;
    esac
done

# Run main function
# 运行主函数
main "$@"
