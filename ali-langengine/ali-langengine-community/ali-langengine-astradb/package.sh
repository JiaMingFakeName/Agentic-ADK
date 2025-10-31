#!/bin/bash

# AstraDB Module Packaging Script
# AstraDB模块打包脚本

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
    log_info "=== AstraDB Module Packaging ==="
    log_info "=== AstraDB模块打包 ==="
    
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
    
    log_info "Starting packaging process..."
    log_info "开始打包过程..."
    
    # Handle command line arguments
    # 处理命令行参数
    local skip_tests=${SKIP_TESTS:-true}
    local clean_first=${CLEAN_FIRST:-true}
    local create_sources=${CREATE_SOURCES:-true}
    local create_javadoc=${CREATE_JAVADOC:-true}
    
    # Build Maven command
    # 构建Maven命令
    local mvn_cmd=""
    if [ "$clean_first" = "true" ]; then
        mvn_cmd="clean"
        log_info "Cleaning previous build artifacts..."
        log_info "清理之前的构建文件..."
    fi
    
    mvn_cmd="$mvn_cmd package"
    
    if [ "$skip_tests" = "true" ]; then
        mvn_cmd="$mvn_cmd -DskipTests"
        log_info "Skipping tests for faster packaging"
        log_info "跳过测试以加快打包速度"
    else
        log_info "Running tests during packaging"
        log_info "打包期间运行测试"
    fi
    
    # Add source and javadoc packaging if requested
    # 如果需要，添加源码和javadoc打包
    local additional_goals=""
    if [ "$create_sources" = "true" ]; then
        additional_goals="$additional_goals source:jar"
        log_info "Creating source JAR"
        log_info "创建源码JAR"
    fi
    
    if [ "$create_javadoc" = "true" ]; then
        additional_goals="$additional_goals javadoc:jar"
        log_info "Creating Javadoc JAR"
        log_info "创建Javadoc JAR"
    fi
    
    # Execute Maven packaging
    # 执行Maven打包
    log_info "Executing: mvn $mvn_cmd"
    log_info "执行: mvn $mvn_cmd"
    
    if mvn $mvn_cmd; then
        log_success "Main packaging completed successfully"
        log_success "主要打包成功完成"
    else
        log_error "Main packaging failed"
        log_error "主要打包失败"
        exit 1
    fi
    
    # Execute additional goals if specified
    # 如果指定了额外目标，则执行
    if [ -n "$additional_goals" ]; then
        log_info "Creating additional artifacts..."
        log_info "创建额外文件..."
        
        if mvn $additional_goals; then
            log_success "Additional artifacts created successfully"
            log_success "额外文件创建成功"
        else
            log_warning "Failed to create some additional artifacts, but main packaging succeeded"
            log_warning "创建某些额外文件失败，但主要打包成功"
        fi
    fi
    
    # Show packaging results
    # 显示打包结果
    log_info "Packaging results / 打包结果:"
    if [ -d "target" ]; then
        local jar_files=$(ls target/*.jar 2>/dev/null || echo "")
        if [ -n "$jar_files" ]; then
            log_success "Generated JAR files:"
            log_success "生成的JAR文件:"
            ls -la target/*.jar
            
            # Calculate total size
            # 计算总大小
            local total_size=$(du -ch target/*.jar 2>/dev/null | tail -1 | cut -f1 || echo "Unknown")
            log_info "Total size: $total_size"
            log_info "总大小: $total_size"
        else
            log_warning "No JAR files found in target directory"
            log_warning "在target目录中未找到JAR文件"
        fi
        
        # Show other important files
        # 显示其他重要文件
        if [ -d "target/classes" ]; then
            local class_count=$(find target/classes -name "*.class" 2>/dev/null | wc -l || echo "0")
            log_info "Compiled classes: $class_count"
            log_info "编译的类: $class_count"
        fi
    else
        log_error "Target directory not found"
        log_error "未找到target目录"
        exit 1
    fi
    
    log_success "=== AstraDB module packaging completed! ==="
    log_success "=== AstraDB模块打包完成！ ==="
    
    # Show packaging summary
    # 显示打包摘要
    log_info "Packaging Summary / 打包摘要:"
    log_info "  - Module: ali-langengine-astradb"
    log_info "  - Status: Successfully packaged"
    log_info "  - Tests: $([ "$skip_tests" = "true" ] && echo "Skipped" || echo "Executed")"
    log_info "  - Clean: $([ "$clean_first" = "true" ] && echo "Yes" || echo "No")"
    log_info "  - Sources: $([ "$create_sources" = "true" ] && echo "Included" || echo "Not included")"
    log_info "  - Javadoc: $([ "$create_javadoc" = "true" ] && echo "Included" || echo "Not included")"
    log_info "  - Output: target/ directory"
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
        --no-sources)
            CREATE_SOURCES=false
            shift
            ;;
        --with-sources)
            CREATE_SOURCES=true
            shift
            ;;
        --no-javadoc)
            CREATE_JAVADOC=false
            shift
            ;;
        --with-javadoc)
            CREATE_JAVADOC=true
            shift
            ;;
        --minimal)
            CREATE_SOURCES=false
            CREATE_JAVADOC=false
            SKIP_TESTS=true
            shift
            ;;
        --full)
            CREATE_SOURCES=true
            CREATE_JAVADOC=true
            SKIP_TESTS=false
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  --with-tests      Run tests during packaging"
            echo "  --skip-tests      Skip tests during packaging (default)"
            echo "  --clean           Clean before packaging (default)"
            echo "  --no-clean        Don't clean before packaging"
            echo "  --with-sources    Create source JAR (default)"
            echo "  --no-sources      Don't create source JAR"
            echo "  --with-javadoc    Create Javadoc JAR (default)"
            echo "  --no-javadoc      Don't create Javadoc JAR"
            echo "  --minimal         Minimal packaging (no sources, no javadoc, skip tests)"
            echo "  --full            Full packaging (with sources, javadoc, and tests)"
            echo "  -h, --help        Show this help message"
            echo ""
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --with-tests      打包期间运行测试"
            echo "  --skip-tests      打包期间跳过测试（默认）"
            echo "  --clean           打包前清理（默认）"
            echo "  --no-clean        打包前不清理"
            echo "  --with-sources    创建源码JAR（默认）"
            echo "  --no-sources      不创建源码JAR"
            echo "  --with-javadoc    创建Javadoc JAR（默认）"
            echo "  --no-javadoc      不创建Javadoc JAR"
            echo "  --minimal         最小打包（无源码、无javadoc、跳过测试）"
            echo "  --full            完整打包（包含源码、javadoc和测试）"
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
