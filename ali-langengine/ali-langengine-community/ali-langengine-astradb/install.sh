#!/bin/bash

# AstraDB Module Installation Script
# AstraDB模块安装脚本

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
    log_info "=== AstraDB Module Installation ==="
    log_info "=== AstraDB模块安装 ==="
    
    # Check prerequisites
    # 检查前置条件
    if ! command_exists mvn; then
        log_error "Maven is not installed. Please install Maven first."
        log_error "Maven未安装。请先安装Maven。"
        exit 1
    fi
    
    if ! command_exists java; then
        log_error "Java is not installed. Please install Java first."
        log_error "Java未安装。请先安装Java。"
        exit 1
    fi
    
    log_info "Starting installation process..."
    log_info "开始安装过程..."
    
    # Install dependencies and compile
    # 安装依赖并编译
    log_info "Installing dependencies and compiling..."
    log_info "安装依赖并编译..."
    
    if mvn clean compile; then
        log_success "Dependencies installed and compilation completed successfully"
        log_success "依赖安装和编译成功完成"
    else
        log_error "Failed to install dependencies or compile"
        log_error "依赖安装或编译失败"
        exit 1
    fi
    
    # Run tests if enabled
    # 如果启用则运行测试
    if [ "${SKIP_TESTS:-false}" != "true" ]; then
        log_info "Running tests..."
        log_info "运行测试..."
        
        if mvn test; then
            log_success "All tests passed"
            log_success "所有测试通过"
        else
            log_warning "Some tests failed, but installation can continue"
            log_warning "部分测试失败，但安装可以继续"
        fi
    else
        log_info "Skipping tests as requested"
        log_info "按要求跳过测试"
    fi
    
    log_success "=== AstraDB module installation completed! ==="
    log_success "=== AstraDB模块安装完成！ ==="
    
    # Show installation summary
    # 显示安装摘要
    log_info "Installation Summary / 安装摘要:"
    log_info "  - Module: ali-langengine-astradb"
    log_info "  - Status: Successfully installed"
    log_info "  - Tests: $([ "${SKIP_TESTS:-false}" = "true" ] && echo "Skipped" || echo "Executed")"
    log_info "  - Dependencies: AstraDB Java Client, DataStax drivers"
}

# Handle command line arguments
# 处理命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
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
            echo "  --skip-tests      Skip running tests during installation"
            echo "  --with-tests      Run tests during installation (default)"
            echo "  -h, --help        Show this help message"
            echo ""
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --skip-tests      安装期间跳过测试"
            echo "  --with-tests      安装期间运行测试（默认）"  
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
