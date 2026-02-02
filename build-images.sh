#!/bin/bash
set -e

echo "Building user-service..."
cd user-service
./mvnw clean package -DskipTests
docker build -t estore/user-service:latest .
cd ..

echo "Building product-service..."
cd product-service
./mvnw clean package -DskipTests
docker build -t estore/product-service:latest .
cd ..

echo "Building order-service..."
cd order-service
./mvnw clean package -DskipTests
docker build -t estore/order-service:latest .
cd ..

echo "Building api-gateway..."
cd api-gateway
./mvnw clean package -DskipTests
docker build -t estore/api-gateway:latest .
cd ..

echo "Loading images into kind cluster..."
kind load docker-image estore/user-service:latest
kind load docker-image estore/product-service:latest
kind load docker-image estore/order-service:latest
kind load docker-image estore/api-gateway:latest

echo "Done!"
