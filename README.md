# Real-Time Order Tracking System

A comprehensive real-time order tracking dashboard that monitors delivery orders and rider performance with live updates. Built with Spring Boot and React, featuring event-driven architecture and real-time data streaming.

## 🚀 Features

- **Real-Time Updates**: Live order status tracking via Server-Sent Events (SSE)
- **Rider Performance Analytics**: Automated daily summary generation and metrics
- **Event-Driven Architecture**: Kafka-based message processing for scalability
- **Live Dashboard**: React-based web interface with automatic updates
- **Order Management**: Complete order lifecycle tracking from pickup to delivery
- **Performance Monitoring**: Track delivery times, delays, and rider efficiency

## 🏗️ Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.2.3 with Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Message Queue**: Apache Kafka for event processing
- **Real-Time Communication**: Server-Sent Events (SSE)
- **API**: RESTful endpoints with validation

### Frontend (React)
- **Framework**: React 18.2.0
- **Build Tool**: Create React App
- **Real-Time Updates**: EventSource for SSE connection
- **State Management**: React Hooks
- **Styling**: Custom CSS with responsive design

## 📋 Prerequisites

- **Java 17** or higher
- **Node.js 16** or higher
- **PostgreSQL 12** or higher
- **Apache Kafka 2.8** or higher
- **Maven 3.6** or higher

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/real-time-order-tracking.git
cd real-time-order-tracking
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE order_tracking_db;

-- Update connection details in ordertracking_backend/src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/order_tracking_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Start Kafka
```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Server
bin/kafka-server-start.sh config/server.properties

# Create topic (optional - auto-created)
bin/kafka-topics.sh --create --topic order-status-updates --bootstrap-server localhost:9092
```

### 4. Backend Setup
```bash
cd ordertracking_backend

# Install dependencies and run
./mvnw clean install
./mvnw spring-boot:run
```

### 5. Frontend Setup
```bash
cd ordertracking_ui

# Install dependencies
npm install

# Start development server
npm start
```

## 🌐 Access Points

- **Dashboard**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **SSE Stream**: http://localhost:8080/stream/order-updates

## 📡 API Endpoints

### Event Submission
```http
# Submit order status update (JSON body)
POST /api/rider/{riderId}/orders/update
Content-Type: application/json

{
  "orderId": "ORD-001",
  "status": "DELIVERED",
  "notes": "Package delivered to front door"
}

# Quick status update (URL parameters)
POST /api/rider/{riderId}/orders/{orderId}/status/{status}
```

### Dashboard Data
```http
# Get today's rider performance summary
GET /metrics/daily-summary

# Real-time updates stream
GET /stream/order-updates
```

### Order Status Values
- `PICKED_UP` - Order picked up by rider
- `IN_TRANSIT` - Order in transit to destination
- `DELIVERED` - Order successfully delivered

## 🎯 Usage Examples

### Submit Order Event (cURL)
```bash
# Update order status
curl -X POST http://localhost:8080/api/rider/RID-001/orders/update \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-12345",
    "status": "DELIVERED",
    "notes": "Delivered to customer"
  }'

# Quick status update
curl -X POST http://localhost:8080/api/rider/RID-001/orders/ORD-12345/status/DELIVERED
```

### Real-Time Dashboard
1. Open http://localhost:3000
2. Submit order events via API
3. Watch live updates appear on dashboard
4. Monitor rider performance metrics

## 🏢 Project Structure

```
├── ordertracking_backend/          # Spring Boot backend
│   ├── src/main/java/com/ordertracking/
│   │   ├── controller/             # REST API endpoints
│   │   ├── service/                # Business logic
│   │   ├── model/                  # JPA entities
│   │   ├── repository/             # Data access layer
│   │   ├── dto/                    # Data transfer objects
│   │   └── config/                 # Configuration classes
│   └── src/main/resources/
│       └── application.properties  # Configuration
└── ordertracking_ui/               # React frontend
    ├── src/
    │   ├── components/             # React components
    │   ├── App.js                  # Main application
    │   └── index.css               # Styling
    └── public/                     # Static assets
```

## 🔧 Configuration

### Backend Configuration (application.properties)
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/order_tracking_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=order-tracking-group
order.kafka.topic.order-updates=order-status-updates

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Frontend Configuration
The frontend automatically proxies API calls to the backend during development.

## 🚀 Production Deployment

### Backend
```bash
# Build JAR
./mvnw clean package

# Run production JAR
java -jar target/real-time-order-tracking-1.0.0.jar
```

### Frontend
```bash
# Build for production
npm run build

# Serve static files (nginx, apache, etc.)
# Files will be in the 'build' directory
```

## 🧪 Testing

### Backend Tests
```bash
cd ordertracking_backend
./mvnw test
```

### Frontend Tests
```bash
cd ordertracking_ui
npm test
```

## 📊 Key Features Explained

### Real-Time Updates
- Uses Server-Sent Events (SSE) for live dashboard updates
- Automatic reconnection on connection loss
- 10-second polling for rider statistics

### Event Processing
- Kafka-based event streaming for scalability
- Automatic daily summary generation on delivery events
- Duplicate event prevention and deduplication

### Performance Analytics
- Daily rider performance summaries
- Delivery time tracking and averages
- Delay monitoring and reporting

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue on GitHub
- Check the [documentation](docs/)
- Review the API endpoints above

## 🔮 Future Enhancements

- [ ] Mobile app for riders
- [ ] Advanced analytics and reporting
- [ ] Route optimization
- [ ] Customer notifications
- [ ] Multi-tenant support
- [ ] Docker containerization

---
