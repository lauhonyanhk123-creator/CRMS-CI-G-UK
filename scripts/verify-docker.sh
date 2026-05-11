#!/bin/bash

#==============================================================================
# CRMS CI G UK - Docker Setup Verification Script
# 
# This script verifies that Docker is properly installed and configured
# for running the CRMS CI G UK system.
#
# Usage:
#   chmod +x scripts/verify-docker.sh
#   ./scripts/verify-docker.sh
#
#==============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counter for tests
TESTS_PASSED=0
TESTS_FAILED=0

# Print functions
print_header() {
    echo ""
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
    ((TESTS_PASSED++))
}

print_error() {
    echo -e "${RED}✗${NC} $1"
    ((TESTS_FAILED++))
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Main verification function
main() {
    print_header "CRMS CI G UK - Docker Setup Verification"
    echo "This script will verify your system is ready for CRMS."
    echo ""

    # Check 1: Operating System
    print_header "1. Operating System Check"
    OS=$(uname -s)
    if [ "$OS" = "Linux" ] || [ "$OS" = "Darwin" ] || [[ "$OS" == *"MINGW"* ]]; then
        print_success "Operating System: $OS"
    else
        print_error "Unsupported Operating System: $OS"
        print_info "Supported: Linux, macOS, Windows (WSL2)"
    fi

    # Check 2: Docker
    print_header "2. Docker Installation"
    
    if command_exists docker; then
        DOCKER_VERSION=$(docker --version | grep -oP '\d+\.\d+\.\d+' | head -1)
        print_success "Docker installed: version $DOCKER_VERSION"
        
        # Check if Docker daemon is running
        if docker info >/dev/null 2>&1; then
            print_success "Docker daemon is running"
        else
            print_error "Docker daemon is not running"
            print_info "Start Docker with: sudo systemctl start docker"
        fi
    else
        print_error "Docker is not installed"
        print_info "Install Docker: https://docs.docker.com/get-docker/"
    fi

    # Check 3: Docker Compose
    print_header "3. Docker Compose Installation"
    
    if command_exists docker; then
        COMPOSE_VERSION=$(docker compose version --short 2>/dev/null || docker-compose --version | grep -oP '\d+\.\d+\.\d+' | head -1)
        if [ -n "$COMPOSE_VERSION" ]; then
            print_success "Docker Compose installed: version $COMPOSE_VERSION"
        else
            print_warning "Docker Compose version could not be determined"
        fi
    else
        print_error "Docker Compose not available (Docker not installed)"
    fi

    # Check 4: Git
    print_header "4. Git Installation"
    
    if command_exists git; then
        GIT_VERSION=$(git --version | grep -oP '\d+\.\d+\.\d+')
        print_success "Git installed: version $GIT_VERSION"
    else
        print_warning "Git is not installed"
        print_info "Install Git: https://git-scm.com/downloads"
    fi

    # Check 5: System Resources
    print_header "5. System Resources"
    
    # Memory
    if command_exists free; then
        TOTAL_MEM=$(free -g | awk '/^Mem:/{print $2}')
        if [ "$TOTAL_MEM" -ge 8 ]; then
            print_success "Memory: ${TOTAL_MEM}GB (meets minimum requirement)"
        else
            print_warning "Memory: ${TOTAL_MEM}GB (minimum 8GB recommended)"
        fi
    fi

    # CPU
    if command_exists nproc; then
        CPU_CORES=$(nproc)
        if [ "$CPU_CORES" -ge 2 ]; then
            print_success "CPU cores: $CPU_CORES (meets minimum requirement)"
        else
            print_warning "CPU cores: $CPU_CORES (minimum 2 cores recommended)"
        fi
    fi

    # Disk Space
    if command_exists df; then
        AVAILABLE_SPACE=$(df -h . | awk 'NR==2 {print $4}')
        print_info "Available disk space: $AVAILABLE_SPACE"
    fi

    # Check 6: Port Availability
    print_header "6. Port Availability"
    
    PORTS=("5173" "8080" "5432" "6379" "9000" "9001" "8025")
    ALL_PORTS_FREE=true
    
    for PORT in "${PORTS[@]}"; do
        if command_exists lsof; then
            if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
                print_warning "Port $PORT is already in use"
                ALL_PORTS_FREE=false
            else
                print_success "Port $PORT is available"
            fi
        else
            print_info "Port check skipped (lsof not available)"
            break
        fi
    done

    # Check 7: Repository Files
    print_header "7. Repository Structure"
    
    if [ -f "docker/docker-compose.yml" ]; then
        print_success "Found: docker/docker-compose.yml"
    else
        print_error "Missing: docker/docker-compose.yml"
    fi
    
    if [ -f "docker/.env.example" ]; then
        print_success "Found: docker/.env.example"
    else
        print_warning "Missing: docker/.env.example"
    fi
    
    if [ -f "backend/pom.xml" ]; then
        print_success "Found: backend/pom.xml"
    else
        print_error "Missing: backend/pom.xml"
    fi
    
    if [ -f "frontend/package.json" ]; then
        print_success "Found: frontend/package.json"
    else
        print_error "Missing: frontend/package.json"
    fi

    # Check 8: Docker Resources
    print_header "8. Docker Configuration"
    
    if docker info >/dev/null 2>&1; then
        # Check Docker disk usage
        DOCKER_USAGE=$(docker system df --format '{{.Type}}\t{{.Size}}' 2>/dev/null | head -5)
        if [ -n "$DOCKER_USAGE" ]; then
            print_info "Docker disk usage:"
            echo "$DOCKER_USAGE" | while IFS=$'\t' read -r type size; do
                echo "    $type: $size"
            done
        fi
        
        # Check if Docker can pull images
        if docker pull hello-world >/dev/null 2>&1; then
            print_success "Docker can pull images"
            docker rmi hello-world >/dev/null 2>&1
        else
            print_warning "Docker cannot pull images (check internet connection)"
        fi
    fi

    # Summary
    print_header "Verification Summary"
    echo ""
    echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
    echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
    echo ""

    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "${GREEN}✓ Your system is ready for CRMS CI G UK!${NC}"
        echo ""
        echo "Next steps:"
        echo "  1. cd docker"
        echo "  2. cp .env.example .env"
        echo "  3. docker compose up -d"
        echo "  4. Access http://localhost:5173"
    else
        echo -e "${YELLOW}⚠ Some checks failed. Review the output above.${NC}"
        echo ""
        echo "Common solutions:"
        echo "  - Install missing software"
        echo "  - Start Docker daemon"
        echo "  - Free up ports"
        echo "  - Clone repository: git clone <repo-url>"
    fi
    
    echo ""
    echo "For help, see: https://github.com/lauhonyanhk123-creator/CRMS-CI-G-UK"
    echo ""
}

# Run main function
main "$@"
