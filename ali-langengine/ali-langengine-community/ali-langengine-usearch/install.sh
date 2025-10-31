#!/bin/bash

# USearch Module Installation Script
# USearch模块安装脚本

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
    log_info "=== USearch Module Installation ==="
    log_info "=== USearch模块安装 ==="
    
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
    
    # Check if dependencies are installed
    # 检查依赖是否已安装
    local usearch_jar="lib/usearch-2.21.0.jar"
    if [ ! -f "$usearch_jar" ]; then
        log_warning "USearch JAR not found at: $usearch_jar"
        log_warning "USearch JAR未找到：$usearch_jar"
        log_info "Installing dependencies first..."
        log_info "先安装依赖..."
        
        if [ -f "dependency.sh" ]; then
            bash dependency.sh
        else
            log_error "dependency.sh not found. Please install dependencies first."
            log_error "dependency.sh未找到。请先安装依赖。"
            exit 1
        fi
    else
        log_info "USearch JAR found: $usearch_jar"
        log_info "找到USearch JAR: $usearch_jar"
    fi
    
    log_info "Starting installation process..."
    log_info "开始安装过程..."
    
    # Handle command line arguments for test execution
    # 处理测试执行的命令行参数
    local skip_tests=${SKIP_TESTS:-true}
    local clean_first=${CLEAN_FIRST:-true}
    
    # Build Maven command
    # 构建Maven命令
    local mvn_cmd=""
    if [ "$clean_first" = "true" ]; then
        mvn_cmd="clean"
    fi
    mvn_cmd="$mvn_cmd compile install"
    
    if [ "$skip_tests" = "true" ]; then
        mvn_cmd="$mvn_cmd -DskipTests"
        log_info "Skipping tests for faster installation"
        log_info "跳过测试以加快安装速度"
    else
        log_info "Running tests during installation"
        log_info "安装期间运行测试"
    fi
    
    # Execute Maven installation
    # 执行Maven安装
    log_info "Executing: mvn $mvn_cmd"
    log_info "执行: mvn $mvn_cmd"
    
    if mvn $mvn_cmd; then
        log_success "Installation completed successfully"
        log_success "安装成功完成"
    else
        log_error "Installation failed"
        log_error "安装失败"
        exit 1
    fi
    
    # Verify installation
    # 验证安装
    log_info "Verifying installation..."
    log_info "验证安装..."
    
    local target_jar="target/ali-langengine-usearch-*.jar"
    if ls $target_jar > /dev/null 2>&1; then
        log_success "JAR file generated successfully"
        log_success "JAR文件生成成功"
        log_info "Generated files:"
        log_info "生成的文件:"
        ls -la target/*.jar 2>/dev/null || true
    else
        log_warning "JAR file not found in target directory"
        log_warning "在target目录中未找到JAR文件"
    fi
    
    # Check if module is available in local Maven repository
    # 检查模块是否在本地Maven仓库中可用
    if mvn dependency:get -Dartifact=com.alibaba:ali-langengine-usearch:1.2.6-202508111516 -q > /dev/null 2>&1; then
        log_success "Module available in local Maven repository"
        log_success "模块在本地Maven仓库中可用"
    else
        log_info "Module may not be fully installed in local repository (this might be normal)"
        log_info "模块可能未完全安装到本地仓库中（这可能是正常的）"
    fi
    
    log_success "=== USearch module installation completed! ==="
    log_success "=== USearch模块安装完成！ ==="
    
    # Show installation summary
    # 显示安装摘要
    log_info "Installation Summary / 安装摘要:"
    log_info "  - Module: ali-langengine-usearch"
    log_info "  - Status: Successfully installed"
    log_info "  - Tests: $([ "$skip_tests" = "true" ] && echo "Skipped" || echo "Executed")"
    log_info "  - Clean: $([ "$clean_first" = "true" ] && echo "Yes" || echo "No")"
    log_info "  - Local repository: Updated"
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
        --no-clean)
            CLEAN_FIRST=false
            shift
            ;;
        --clean)
            CLEAN_FIRST=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  --with-tests    Run tests during installation"
            echo "  --skip-tests    Skip tests during installation (default)"
            echo "  --clean         Clean before installation (default)"
            echo "  --no-clean      Don't clean before installation"
            echo "  -h, --help      Show this help message"
            echo ""
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --with-tests    安装期间运行测试"
            echo "  --skip-tests    安装期间跳过测试（默认）"
            echo "  --clean         安装前清理（默认）"
            echo "  --no-clean      安装前不清理"
            echo "  -h, --help      显示此帮助信息"
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
