#!/bin/bash

# Docker management scripts for Order Tracking System

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Development environment
dev_up() {
    print_status "Starting development environment..."
    docker-compose up -d
    print_status "Development environment started!"
    print_status "Frontend: http://localhost:3000"
    print_status "Backend API: http://localhost:8080"
    print_status "Kafka UI: http://localhost:8081"
}

dev_down() {
    print_status "Stopping development environment..."
    docker-compose down
    print_status "Development environment stopped!"
}

dev_logs() {
    docker-compose logs -f
}

# Production environment
prod_up() {
    print_status "Starting production environment..."
    if [ ! -f .env ]; then
        print_warning "No .env file found. Creating from .env.example..."
        cp .env.example .env
        print_warning "Please edit .env file with your production settings!"
        return 1
    fi
    docker-compose -f docker-compose.prod.yml up -d
    print_status "Production environment started!"
}

prod_down() {
    print_status "Stopping production environment..."
    docker-compose -f docker-compose.prod.yml down
    print_status "Production environment stopped!"
}

prod_logs() {
    docker-compose -f docker-compose.prod.yml logs -f
}

# Build images
build() {
    print_status "Building Docker images..."
    docker-compose build --no-cache
    print_status "Docker images built successfully!"
}

# Clean up
clean() {
    print_status "Cleaning up Docker resources..."
    docker-compose down -v --remove-orphans
    docker system prune -f
    print_status "Cleanup completed!"
}

# Health check
health() {
    print_status "Checking service health..."
    
    # Check if containers are running
    if docker-compose ps | grep -q "Up"; then
        print_status "Containers are running"
        
        # Check backend health
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            print_status "Backend is healthy"
        else
            print_error "Backend health check failed"
        fi
        
        # Check frontend health
        if curl -f http://localhost:3000 >/dev/null 2>&1; then
            print_status "Frontend is accessible"
        else
            print_error "Frontend health check failed"
        fi
        
        # Check Kafka UI health
        if curl -f http://localhost:8081 >/dev/null 2>&1; then
            print_status "Kafka UI is accessible"
        else
            print_error "Kafka UI health check failed"
        fi
    else
        print_error "Containers are not running"
    fi
}

# Show usage
usage() {
    echo "Usage: $0 {dev-up|dev-down|dev-logs|prod-up|prod-down|prod-logs|build|clean|health}"
    echo ""
    echo "Commands:"
    echo "  dev-up      Start development environment"
    echo "  dev-down    Stop development environment"
    echo "  dev-logs    Show development logs"
    echo "  prod-up     Start production environment"
    echo "  prod-down   Stop production environment"
    echo "  prod-logs   Show production logs"
    echo "  build       Build Docker images"
    echo "  clean       Clean up Docker resources"
    echo "  health      Check service health"
}

# Main script logic
case "$1" in
    dev-up)
        dev_up
        ;;
    dev-down)
        dev_down
        ;;
    dev-logs)
        dev_logs
        ;;
    prod-up)
        prod_up
        ;;
    prod-down)
        prod_down
        ;;
    prod-logs)
        prod_logs
        ;;
    build)
        build
        ;;
    clean)
        clean
        ;;
    health)
        health
        ;;
    *)
        usage
        exit 1
        ;;
esac