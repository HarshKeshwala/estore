# estore

## Overview

A simple e-commerce system built with Spring Boot, Kafka, Spring Cloud API Gateway, and Kubernetes.

## Architecture Diagram

```
                         +------------------+
                         |     Client       |
                         +--------+---------+
                                  |
                                  v
                    +---------------------------+
                    |   Spring Cloud Gateway    |
                    |       (Port: 8080)        |
                    |  - JWT Validation         |
                    |  - Route to services      |
                    +-------------+-------------+
                                  |
        +-------------------------+-------------------------+
        |                         |                         |
        v                         v                         v
+---------------+        +----------------+        +----------------+
| user-service  |        | product-service|        | order-service  |
|  (Port: 8081) |        |  (Port: 8082)  |        |  (Port: 8083)  |
|               |        |                |        |                |
| - Auth (JWT)  |        | - Products CRUD|        | - Cart mgmt    |
| - User mgmt   |        | - Inventory    |        | - Order mgmt   |
| - Roles       |        | - Kafka consumer|       | - Kafka producer|
+-------+-------+        +-------+--------+        +-------+--------+
        |                        |                         |
        v                        v                         v
   H2 (userdb)             H2 (productdb)            H2 (orderdb)
                                 ^
                                 |
                    +------------+------------+
                    |       Apache Kafka      |
                    |  Topic: inventory-update|
                    +-------------------------+
```

## Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 4.0 |
| Gateway | Spring Cloud Gateway |
| Database | H2 (in-memory) |
| Messaging | Apache Kafka |
| Authentication | JWT |
| Container Runtime | Docker |
| Orchestration | Kubernetes (kind) |

## Services

### API Gateway (Port 8080)

Routes all requests and validates JWT for protected endpoints.

| Route | Target | Auth Required |
|-------|--------|---------------|
| `/api/auth/**` | user-service | No |
| `/api/users/**` | user-service | Yes |
| `/api/products/**` | product-service | Yes |
| `/api/cart/**` | order-service | Yes |
| `/api/orders/**` | order-service | Yes |

### user-service (Port 8081)

Handles user authentication and management.

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Register user |
| POST | `/auth/login` | No | Login, get JWT |
| POST | `/auth/refresh` | Bearer | Refresh token |
| GET | `/users/me` | Bearer | Get profile |
| PUT | `/users/me` | Bearer | Update profile |

### product-service (Port 8082)

Manages product catalog and inventory.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/products` | Any | List products |
| GET | `/products/{id}` | Any | Get product |
| POST | `/products` | ADMIN | Create product |
| PUT | `/products/{id}` | ADMIN | Update product |
| DELETE | `/products/{id}` | ADMIN | Delete product |

### order-service (Port 8083)

Manages shopping cart and orders.

**Cart Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cart` | Get user's cart |
| POST | `/cart/items` | Add item |
| PUT | `/cart/items/{id}` | Update quantity |
| DELETE | `/cart/items/{id}` | Remove item |
| DELETE | `/cart` | Clear cart |

**Order Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Place order |
| GET | `/orders` | Order history |
| GET | `/orders/{id}` | Order details |

## Authentication Flow

1. User authenticates via `/api/auth/login`
2. user-service returns JWT token with claims (userId, email, role)
3. Client includes JWT in `Authorization: Bearer <token>` header
4. API Gateway validates JWT and extracts claims
5. Gateway forwards request with headers: `X-User-Id`, `X-User-Email`, `X-User-Role`
6. Backend services trust these headers (no JWT validation needed for now)

## Kafka Integration

**Topic:** `inventory-update`

**Flow:**
1. User places order via `POST /orders`
2. order-service validates stock (HTTP call to product-service)
3. order-service creates order, publishes `InventoryUpdateEvent`
4. product-service consumes event, decrements stock

**Event Schema:**
```java
public record InventoryUpdateEvent(
    String eventId,
    Long productId,
    int quantityChange,
    Long orderId,
    LocalDateTime timestamp
) {}
```

## Service Communication

- **Gateway to Services:** HTTP (via Spring Cloud Gateway routes)
- **Service to Service:** RestClient (Spring 6.1+)
- **Async Events:** Apache Kafka

## Kubernetes Deployment

Services are deployed to a kind (Kubernetes IN Docker) cluster.

**Port Mapping:**
| Service | Internal Port | External Port |
|---------|--------------|---------------|
| API Gateway | 8080 | 30080 (NodePort) |
| user-service | 8081 | - |
| product-service | 8082 | - |
| order-service | 8083 | - |
| Kafka | 9092 | - |

## Local Development

### Run Services Locally
### Build and load images, Deploy to Kind

```bash
# Build and load images, deploy to kind
./deploy-kind.sh
```
