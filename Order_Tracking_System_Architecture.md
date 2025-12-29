# Real-Time Order Tracking System - Architecture

## System Overview
Event-driven order tracking system with real-time dashboard updates via SSE and Kafka message processing.

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   React UI      │◄──►│  Spring Boot API │◄──►│   PostgreSQL    │
│   (Port 3000)   │    │   (Port 8080)    │    │   (Port 5432)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        
         │ SSE Stream              │ Kafka Producer/Consumer                       
         │                        ▼                        
         │              ┌──────────────────┐               
         │              │  Apache Kafka    │               
         │              │   (Port 9092)    │               
         │              └──────────────────┘               
         │                        │                        
         │                        │ Monitoring                       
         │              ┌──────────────────┐               
         └─────────────►│   Kafka UI       │               
                        │   (Port 8081)    │               
                        └──────────────────┘               
```

## Technology Stack
- **Backend**: Spring Boot 3.2.3, Java 17, PostgreSQL 15, Kafka 7.4.0
- **Frontend**: React 18.2.0, EventSource SSE
- **Infrastructure**: Docker Compose, Nginx

## Database Schema

### order_events
```sql
CREATE TABLE order_events (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    rider_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PICKED_UP', 'IN_TRANSIT', 'DELIVERED')),
    event_timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_order_events_order_id ON order_events(order_id);
CREATE INDEX idx_order_events_rider_id ON order_events(rider_id);
```

### order_daily_summary
```sql
CREATE TABLE order_daily_summary (
    id BIGSERIAL PRIMARY KEY,
    rider_id VARCHAR(50) NOT NULL,
    summary_date DATE NOT NULL,
    delivered_orders INTEGER DEFAULT 0,
    avg_delivery_time_minutes INTEGER,
    delayed_orders INTEGER DEFAULT 0,
    UNIQUE(rider_id, summary_date)
);
```

## API Endpoints

### Rider Operations
```http
# Full order update
POST /api/rider/{riderId}/orders/update
{
    "orderId": "ORD-12345",
    "status": "DELIVERED",
    "notes": "Optional notes",
    "eventTimestamp": "2024-01-15T14:30:00"
}

# Quick status update
POST /api/rider/{riderId}/orders/{orderId}/status/{status}
```

### Metrics
```http
GET /metrics/daily-summary?date=2024-01-15
```

### Real-Time Stream
```http
GET /stream/order-updates
Accept: text/event-stream
```

## Message Flow

### Kafka Topic: `order-status-updates`
```
Rider API → Kafka Producer → Topic → Kafka Consumer → SSE Service → Frontend
```

### Event Processing
1. **OrderService**: Validates and publishes events to Kafka
2. **KafkaConsumerService**: Consumes events and forwards to SSE
3. **SSEService**: Broadcasts to connected frontend clients
4. **AggregationService**: Daily stats calculation (scheduled 23:00)

## Data Models

### DTOs
```java
// Input from riders
RiderEventDTO {
    orderId: String,
    status: "PICKED_UP|IN_TRANSIT|DELIVERED",
    notes: String,
    eventTimestamp: LocalDateTime
}

// Internal processing
OrderEventDTO {
    orderId: String,
    riderId: String,
    status: String,
    eventTimestamp: LocalDateTime
}

// SSE output
SSEEventDTO {
    eventType: "ORDER_UPDATE",
    orderId: String,
    riderId: String,
    status: String,
    message: String,
    eventTimestamp: LocalDateTime
}
```

## Service Architecture

### Backend Services
- **OrderService**: Event validation and Kafka publishing
- **SSEService**: Real-time client connection management
- **AggregationService**: Daily rider performance calculations
- **KafkaConsumerService**: Message processing and SSE forwarding

### Frontend Components
```
App.js (State management, SSE connection)
├── ActivityBanner (Live status)
├── OrderTable (Orders grid)
├── RiderStats (Performance metrics)
└── RecentEvents (Activity timeline)
```

## Configuration

### Key Properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://postgres:5432/order_tracking_db
spring.jpa.hibernate.ddl-auto=update

# Kafka
spring.kafka.bootstrap-servers=kafka:29092
spring.kafka.consumer.group-id=order-tracking-group
order.kafka.topic.order-updates=order-status-updates

# SSE & Scheduling
sse.keep-alive-interval=30000
aggregation.cron=0 0 23 * * *
```

### Docker Services
- **postgres**: Database with persistent volumes
- **kafka/zookeeper**: Message broker setup
- **backend**: Spring Boot API
- **frontend**: React app via Nginx
- **kafka-ui**: Monitoring interface

## Security & Performance
- **Validation**: Bean validation on DTOs
- **CORS**: Configured for frontend-backend communication
- **Indexing**: Database indexes on frequently queried columns
- **Connection Pooling**: HikariCP for database connections
- **Health Checks**: Docker container monitoring

This architecture provides real-time order tracking with event-driven processing, scalable message queues, and live dashboard updates.