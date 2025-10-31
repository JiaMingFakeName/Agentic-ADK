#!/bin/bash

# USearch Module Dependency Installation Script
# USearch模块依赖安装脚本

# Enable strict error handling - exit on any command failure
# 启用严格错误处理 - 任何命令失败时退出
set -e
set -o pipefail

# Configuration
# 配置
USEARCH_VERSION="2.21.0"
USEARCH_JAR="usearch-${USEARCH_VERSION}.jar"
LIB_DIR="lib"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}"

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

# Download file with progress
# 带进度条下载文件
download_file() {
    local url="$1"
    local output="$2"
    local description="$3"
    
    log_info "Downloading $description..."
    log_info "URL: $url"
    log_info "Output: $output"
    
    if command_exists curl; then
        curl -L --progress-bar --fail "$url" -o "$output"
    elif command_exists wget; then
        wget --progress=bar:force "$url" -O "$output"
    else
        log_error "Neither curl nor wget is available. Please install one of them."
        exit 1
    fi
}

# Create directory if it doesn't exist
# 如果目录不存在则创建
ensure_directory() {
    local dir="$1"
    if [ ! -d "$dir" ]; then
        log_info "Creating directory: $dir"
        mkdir -p "$dir"
    fi
}

# Check if USearch JAR exists and is valid
# 检查USearch JAR是否存在且有效
check_usearch_jar() {
    local jar_path="$1"
    
    if [ ! -f "$jar_path" ]; then
        return 1
    fi
    
    # Check if it's a valid JAR file
    # 检查是否为有效的JAR文件
    if command_exists file; then
        local file_type=$(file "$jar_path" 2>/dev/null || echo "")
        if [[ "$file_type" == *"Java archive"* ]] || [[ "$file_type" == *"Zip archive"* ]]; then
            local size=$(stat -f%z "$jar_path" 2>/dev/null || stat -c%s "$jar_path" 2>/dev/null || echo "0")
            if [ "$size" -gt 1000 ]; then  # At least 1KB
                return 0
            fi
        fi
    else
        # Fallback: check file size only
        # 回退方案：仅检查文件大小
        local size=$(stat -f%z "$jar_path" 2>/dev/null || stat -c%s "$jar_path" 2>/dev/null || echo "0")
        if [ "$size" -gt 1000 ]; then  # At least 1KB
            return 0
        fi
    fi
    
    return 1
}

# Download USearch JAR
# 下载USearch JAR
download_usearch_jar() {
    local lib_path="${PROJECT_ROOT}/${LIB_DIR}"
    local jar_path="${lib_path}/${USEARCH_JAR}"
    
    ensure_directory "$lib_path"
    
    # Check if JAR already exists and is valid
    # 检查JAR是否已存在且有效
    if check_usearch_jar "$jar_path"; then
        log_success "USearch JAR already exists and is valid: $jar_path"
        return 0
    fi
    
    log_info "USearch JAR not found or invalid, attempting to download..."
    
    # Try multiple download sources
    # 尝试多个下载源
    local urls=(
        "https://repo1.maven.org/maven2/cloud/unum/usearch/${USEARCH_VERSION}/usearch-${USEARCH_VERSION}.jar"
        "https://search.maven.org/remotecontent?filepath=cloud/unum/usearch/${USEARCH_VERSION}/usearch-${USEARCH_VERSION}.jar"
        "https://github.com/unum-cloud/usearch/releases/download/v${USEARCH_VERSION}/usearch-java-${USEARCH_VERSION}.jar"
    )
    
    local download_success=false
    
    for url in "${urls[@]}"; do
        log_info "Trying to download from: $url"
        
        # Create temporary file
        # 创建临时文件
        local temp_file="${jar_path}.tmp"
        
        if download_file "$url" "$temp_file" "USearch ${USEARCH_VERSION} JAR"; then
            # Verify downloaded file
            # 验证下载的文件
            if check_usearch_jar "$temp_file"; then
                mv "$temp_file" "$jar_path"
                log_success "Successfully downloaded USearch JAR from: $url"
                download_success=true
                break
            else
                log_warning "Downloaded file is not a valid JAR, trying next source..."
                rm -f "$temp_file"
            fi
        else
            log_warning "Failed to download from: $url"
            rm -f "$temp_file"
        fi
    done
    
    if [ "$download_success" = false ]; then
        log_error "Failed to download USearch JAR from all sources."
        log_error "Please manually download the USearch JAR file and place it at: $jar_path"
        log_error "You can try downloading from:"
        for url in "${urls[@]}"; do
            log_error "  - $url"
        done
        log_error "Or visit: https://github.com/unum-cloud/usearch/releases"
        exit 1
    fi
    
    # Verify final JAR
    # 验证最终的JAR
    local jar_size=$(stat -f%z "$jar_path" 2>/dev/null || stat -c%s "$jar_path" 2>/dev/null || echo "0")
    log_success "USearch JAR downloaded successfully"
    log_info "File: $jar_path"
    log_info "Size: $jar_size bytes"
}

# Main execution
# 主执行流程
main() {
    log_info "=== USearch Module Dependency Installation ==="
    log_info "=== USearch模块依赖安装 ==="
    
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
    
    log_info "Working directory: $PROJECT_ROOT"
    
    # Step 1: Download USearch JAR
    # 步骤1：下载USearch JAR
    log_info "Step 1: Downloading USearch dependencies..."
    log_info "步骤1：下载USearch依赖..."
    download_usearch_jar
    
    # Step 2: Install Maven dependencies
    # 步骤2：安装Maven依赖
    log_info "Step 2: Installing Maven dependencies..."
    log_info "步骤2：安装Maven依赖..."
    
    if mvn clean install -DskipTests; then
        log_success "Maven dependencies installed successfully"
        log_success "Maven依赖安装成功"
    else
        log_error "Failed to install Maven dependencies"
        log_error "Maven依赖安装失败"
        exit 1
    fi
    
    # Step 3: Verify installation
    # 步骤3：验证安装
    log_info "Step 3: Verifying installation..."
    log_info "步骤3：验证安装..."
    
    local jar_path="${PROJECT_ROOT}/${LIB_DIR}/${USEARCH_JAR}"
    if check_usearch_jar "$jar_path"; then
        log_success "USearch JAR verification passed"
        log_success "USearch JAR验证通过"
    else
        log_error "USearch JAR verification failed"
        log_error "USearch JAR验证失败"
        exit 1
    fi
    
    if mvn dependency:tree > /dev/null 2>&1; then
        log_success "Maven dependency tree is valid"
        log_success "Maven依赖树有效"
    else
        log_warning "Maven dependency tree check failed, but installation may still work"
        log_warning "Maven依赖树检查失败，但安装可能仍然有效"
    fi
    
    log_success "=== Dependencies installation completed successfully! ==="
    log_success "=== 依赖安装成功完成！ ==="
    
    # Show installation summary
    # 显示安装摘要
    log_info "Installation Summary / 安装摘要:"
    log_info "  - USearch JAR: $jar_path"
    log_info "  - Maven dependencies: Installed"
    log_info "  - Project ready for: compilation, testing, packaging"
}

# Run main function
# 运行主函数
main "$@"
