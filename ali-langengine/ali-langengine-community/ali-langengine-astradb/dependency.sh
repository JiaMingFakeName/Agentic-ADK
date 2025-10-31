#!/bin/bash

# AstraDB Module Dependency Installation Script
# AstraDB模块依赖安装脚本

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
    log_info "=== AstraDB Module Dependency Installation ==="
    log_info "=== AstraDB模块依赖安装 ==="
    
    # Check prerequisites
    # 检查前置条件
    if ! command_exists mvn; then
        log_error "Maven is not installed. Please install Maven first."
        log_error "Maven未安装。请先安装Maven。"
        exit 1
    fi
    
    log_info "Installing AstraDB module dependencies..."
    log_info "安装AstraDB模块依赖..."
    
    # Download and install dependencies
    # 下载并安装依赖
    log_info "Downloading dependencies using Maven..."
    log_info "使用Maven下载依赖..."
    
    if mvn dependency:resolve; then
        log_success "Dependencies downloaded successfully"
        log_success "依赖下载成功"
    else
        log_error "Failed to download dependencies"
        log_error "依赖下载失败"
        exit 1
    fi
    
    # Verify key dependencies
    # 验证关键依赖
    log_info "Verifying key dependencies..."
    log_info "验证关键依赖..."
    
    # Check if AstraDB Java client is available
    # 检查AstraDB Java客户端是否可用
    if mvn dependency:tree | grep -q "astra-db-java"; then
        log_success "AstraDB Java client dependency found"
        log_success "找到AstraDB Java客户端依赖"
    else
        log_warning "AstraDB Java client dependency not found in tree"
        log_warning "在依赖树中未找到AstraDB Java客户端依赖"
    fi
    
    # Check if DataStax driver is available
    # 检查DataStax驱动是否可用
    if mvn dependency:tree | grep -q "java-driver-core"; then
        log_success "DataStax Java driver dependency found"
        log_success "找到DataStax Java驱动依赖"
    else
        log_warning "DataStax Java driver dependency not found in tree"
        log_warning "在依赖树中未找到DataStax Java驱动依赖"
    fi
    
    log_success "=== AstraDB module dependencies installation completed! ==="
    log_success "=== AstraDB模块依赖安装完成！ ==="
    
    # Show dependency summary
    # 显示依赖摘要
    log_info "Dependency Summary / 依赖摘要:"
    log_info "  - AstraDB Java Client: v1.2.7"
    log_info "  - DataStax Java Driver: v4.17.0"
    log_info "  - Apache HttpClient5: v5.2.1"
    log_info "  - Jackson JSON: Latest"
    log_info "  - FastJSON: Ali LangEngine version"
}

# Handle command line arguments
# 处理命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            echo "Usage: $0"
            echo "This script installs all required dependencies for the AstraDB module."
            echo ""
            echo "用法: $0"
            echo "此脚本安装AstraDB模块所需的所有依赖。"
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
