#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

CLUSTER_NAME="estore-cluster"
NAMESPACE="estore"

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    echo_info "Checking prerequisites..."

    if ! command -v docker &> /dev/null; then
        echo_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v kind &> /dev/null; then
        echo_error "Kind is not installed. Please install Kind first."
        echo "  brew install kind (macOS)"
        echo "  sudo apt install kind (Ubuntu)"
        exit 1
    fi

    if ! command -v kubectl &> /dev/null; then
        echo_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi

    if ! docker info &> /dev/null; then
        echo_error "Docker daemon is not running. Please start Docker."
        exit 1
    fi

    echo_info "All prerequisites satisfied."
}

# Delete existing cluster
delete_cluster() {
    if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
        echo_warn "Existing cluster '${CLUSTER_NAME}' found."
        read -p "Delete existing cluster? (y/N): " confirm
        if [[ "$confirm" =~ ^[Yy]$ ]]; then
            echo_info "Deleting existing cluster..."
            kind delete cluster --name ${CLUSTER_NAME}
        else
            echo_error "Cannot proceed with existing cluster. Exiting."
            exit 1
        fi
    fi
}

# Create Kind cluster
create_cluster() {
    echo_info "Creating Kind cluster '${CLUSTER_NAME}' with port mapping..."

    cat <<EOF | kind create cluster --name ${CLUSTER_NAME} --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 8080
    protocol: TCP
EOF

    echo_info "Kind cluster created successfully."
}

# Build service JARs
build_jars() {
    echo_info "Building service JARs..."

    # ./mvnw clean package -DskipTests -q
    echo "Building user-service..."
    cd user-service
    ./mvnw clean package -DskipTests
    cd ..

    echo "Building product-service..."
    cd product-service
    ./mvnw clean package -DskipTests
    cd ..

    echo "Building order-service..."
    cd order-service
    ./mvnw clean package -DskipTests
    cd ..

    echo "Building api-gateway..."
    cd api-gateway
    ./mvnw clean package -DskipTests
    cd ..

    echo_info "JARs built successfully."
}

# Build Docker images
build_images() {
    echo_info "Building Docker images..."

    docker build -t estore/user-service:latest ./user-service
    docker build -t estore/product-service:latest ./product-service
    docker build -t estore/order-service:latest ./order-service
    docker build -t estore/api-gateway:latest ./api-gateway

    echo_info "Docker images built successfully."
}

# Load images into Kind
load_images() {
    echo_info "Loading images into Kind cluster..."

    kind load docker-image estore/user-service:latest --name ${CLUSTER_NAME}
    kind load docker-image estore/product-service:latest --name ${CLUSTER_NAME}
    kind load docker-image estore/order-service:latest --name ${CLUSTER_NAME}
    kind load docker-image estore/api-gateway:latest --name ${CLUSTER_NAME}

    echo_info "Images loaded successfully."
}

# Deploy manifests
deploy_manifests() {
    echo_info "Deploying Kubernetes manifests..."

    echo_info "Creating namespace..."
    kubectl apply -f k8s/namespace.yaml

    echo_info "Creating configmap and secrets..."
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/secrets.yaml

    echo_info "Deploying Kafka..."
    kubectl apply -f k8s/kafka/

    echo_info "Waiting for Kafka to be ready..."
    kubectl wait --for=condition=ready pod -l app=kafka -n ${NAMESPACE} --timeout=180s || echo_warn "Kafka may not be ready yet, continuing..."

    echo_info "Deploying user-service..."
    kubectl apply -f k8s/user-service/

    echo_info "Deploying product-service..."
    kubectl apply -f k8s/product-service/

    echo_info "Deploying order-service..."
    kubectl apply -f k8s/order-service/

    echo_info "Deploying api-gateway..."
    kubectl apply -f k8s/api-gateway/

    echo_info "Manifests deployed successfully."
}

# Wait for pods to be ready
wait_for_pods() {
    echo_info "Waiting for all pods to be ready..."

    echo_info "Waiting for user-service..."
    kubectl wait --for=condition=ready pod -l app=user-service -n ${NAMESPACE} --timeout=180s || echo_warn "user-service may not be ready yet"

    echo_info "Waiting for product-service..."
    kubectl wait --for=condition=ready pod -l app=product-service -n ${NAMESPACE} --timeout=180s || echo_warn "product-service may not be ready yet"

    echo_info "Waiting for order-service..."
    kubectl wait --for=condition=ready pod -l app=order-service -n ${NAMESPACE} --timeout=180s || echo_warn "order-service may not be ready yet"

    echo_info "Waiting for api-gateway..."
    kubectl wait --for=condition=ready pod -l app=api-gateway -n ${NAMESPACE} --timeout=180s || echo_warn "api-gateway may not be ready yet"
}

# Show status
show_status() {
    echo ""
    echo_info "============================================"
    echo_info "Deployment Status"
    echo_info "============================================"
    echo ""
    kubectl get pods -n ${NAMESPACE}
    echo ""
    kubectl get services -n ${NAMESPACE}
    echo ""
    echo_info "============================================"
    echo_info "Access URL: http://localhost:8080"
    echo_info "============================================"
    echo ""
    echo "Test commands:"
    echo "  curl http://localhost:8080/actuator/health"
    echo "  curl http://localhost:8080/api/products"
    echo ""
    echo "View logs:"
    echo "  kubectl logs -f deployment/api-gateway -n ${NAMESPACE}"
    echo "  kubectl logs -f deployment/user-service -n ${NAMESPACE}"
    echo "  kubectl logs -f deployment/product-service -n ${NAMESPACE}"
    echo "  kubectl logs -f deployment/order-service -n ${NAMESPACE}"
    echo ""
}

# Main execution
main() {
    echo_info "E-Store Microservices - Kind Deployment"
    echo ""

    check_prerequisites
    delete_cluster
    create_cluster
    build_jars
    build_images
    load_images
    deploy_manifests
    wait_for_pods
    show_status

    echo_info "Deployment complete!"
}

# Run main
main "$@"
