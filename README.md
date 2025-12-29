# Real-Time Order Tracking System

A real-time order tracking dashboard built with **Spring Boot** and **React**, featuring live updates via Server-Sent Events (SSE) and event-driven architecture with Apache Kafka.

## 🚀 Quick Start

```bash
# Clone the repository
git clone https://github.com/amanullah-3/intern-amanullah-order-tracking.git
cd intern-amanullah-order-tracking

# Start all services with Docker
docker-compose up -d

# Access the application
# Dashboard: http://localhost:3000
# API: http://localhost:8080
# Kafka UI: http://localhost:8081
```

**Note**: First build takes 5-10 minutes to download dependencies. Subsequent starts are much faster.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.2.3 with Java 17
- **Frontend**: React 18.2.0 with real-time SSE updates
- **Database**: PostgreSQL with JPA/Hibernate
- **Message Queue**: Apache Kafka for event processing
- **Monitoring**: Kafka UI for message flow visualization
- **Deployment**: Docker Compose with multi-stage builds

## 📡 API Usage

### Submit Order Events
```bash
# Update order status (JSON)
curl -X POST http://localhost:8080/api/rider/RID-001/orders/update \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "status": "DELIVERED",
    "notes": "Package delivered successfully"
  }'

# Quick status update (URL parameters)
curl -X POST http://localhost:8080/api/rider/RID-001/orders/ORD-12345/status/DELIVERED
```

### Order Status Values
- `PICKED_UP` - Order picked up by rider
- `IN_TRANSIT` - Order in transit to destination  
- `DELIVERED` - Order successfully delivered

### Get Dashboard Data
```bash
# Get today's rider performance summary
curl http://localhost:8080/metrics/daily-summary

# Real-time updates stream
curl http://localhost:8080/stream/order-updates
```

## 🎯 Key Features

- **Real-Time Updates**: Live dashboard updates via Server-Sent Events
- **Event-Driven**: Kafka-based message processing for scalability
- **Automated Analytics**: Daily rider performance summaries
- **Live Monitoring**: Kafka UI for debugging and monitoring
- **Responsive Design**: Clean, modern web interface

## 🛠️ Development

### Prerequisites
- Docker & Docker Compose
- Git

### Local Development
```bash
# Start development environment
docker-compose up -d

# View logs
docker-compose logs -f

# Stop environment
docker-compose down
```

### Manual Setup (Optional)
If you prefer to run without Docker:
- Java 17+, Node.js 16+, PostgreSQL 12+, Apache Kafka 2.8+
- See individual service directories for setup instructions

## 📊 Services

| Service | Port | Description |
|---------|------|-------------|
| Frontend | 3000 | React dashboard |
| Backend | 8080 | Spring Boot API |
| Kafka UI | 8081 | Message monitoring |
| PostgreSQL | 5432 | Database |
| Kafka | 9092 | Message broker |

## 🏢 Project Structure

```
├── ordertracking_backend/     # Spring Boot API
│   ├── src/main/java/        # Java source code
│   ├── Dockerfile            # Backend container
│   └── pom.xml              # Maven dependencies
├── ordertracking_ui/         # React frontend
│   ├── src/                 # React components
│   ├── Dockerfile           # Frontend container
│   └── package.json         # NPM dependencies
├── docker-compose.yml        # Service orchestration
└── README.md                # This file
```

## 🧪 Testing the System

1. **Start the application**: `docker-compose up -d`
2. **Open dashboard**: http://localhost:3000
3. **Submit test events**:
   ```bash
   curl -X POST http://localhost:8080/api/rider/RID-001/orders/ORD-001/status/PICKED_UP
   curl -X POST http://localhost:8080/api/rider/RID-001/orders/ORD-001/status/IN_TRANSIT
   curl -X POST http://localhost:8080/api/rider/RID-001/orders/ORD-001/status/DELIVERED
   ```
4. **Watch live updates** on the dashboard
5. **Monitor Kafka**: http://localhost:8081
