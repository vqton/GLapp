#!/bin/bash
# ============================================================
# AccountingERP - Quick Commands Shell Script (Linux/Mac)
# ============================================================
# Usage: chmod +x run.sh && ./run.sh
# ============================================================

show_menu() {
    echo ""
    echo "============================================================"
    echo "    AccountingERP - Quick Commands"
    echo "============================================================"
    echo ""
    echo "1. Build Project (mvn clean install)"
    echo "2. Build without tests (mvn clean install -DskipTests)"
    echo "3. Run in Dev Mode (H2 Database)"
    echo "4. Run in Prod Mode (PostgreSQL)"
    echo "5. Run on different port"
    echo "6. Clean project"
    echo "7. Run tests"
    echo "8. Show system info"
    echo "9. Exit"
    echo ""
    read -p "Select option (1-9): " choice
    
    case $choice in
        1) build_project ;;
        2) build_skip_tests ;;
        3) run_dev ;;
        4) run_prod ;;
        5) run_custom_port ;;
        6) clean_project ;;
        7) run_tests ;;
        8) show_info ;;
        9) exit 0 ;;
        *) echo "Invalid choice"; show_menu ;;
    esac
}

build_project() {
    echo ""
    echo "Building project with tests..."
    mvn clean install
    read -p "Press Enter to continue..."
    show_menu
}

build_skip_tests() {
    echo ""
    echo "Building project (skipping tests)..."
    mvn clean install -DskipTests
    read -p "Press Enter to continue..."
    show_menu
}

run_dev() {
    echo ""
    echo "Starting application in Dev Mode (H2 Database)..."
    echo "Access: http://localhost:8080"
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
    show_menu
}

run_prod() {
    echo ""
    echo "Starting application in Prod Mode (PostgreSQL)..."
    echo "Make sure PostgreSQL is running!"
    echo "Access: http://localhost:8080"
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
    show_menu
}

run_custom_port() {
    echo ""
    read -p "Enter port number (default 8080): " port
    port=${port:-8080}
    echo "Starting on port $port..."
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$port --spring.profiles.active=dev"
    show_menu
}

clean_project() {
    echo ""
    echo "Cleaning project..."
    mvn clean
    read -p "Press Enter to continue..."
    show_menu
}

run_tests() {
    echo ""
    echo "Running tests..."
    mvn test
    read -p "Press Enter to continue..."
    show_menu
}

show_info() {
    echo ""
    echo "Java version:"
    java -version
    echo ""
    echo "Maven version:"
    mvn -v
    echo ""
    read -p "Press Enter to continue..."
    show_menu
}

# Main
show_menu
