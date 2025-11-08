# 💳 Payment Gateway Simulator

A comprehensive payment gateway simulator built with **Spring Boot (Kotlin)** that mimics real-world payment processing systems like Stripe, Razorpay, and PayPal.

## 🚀 Features

### ✅ Phase 1 - Core MVP Features
- ✅ **Payment Initiation** - POST `/api/payments/initiate`
- ✅ **Async Payment Processing** - Simulates 2-5 second processing delay
- ✅ **Payment Status Check** - GET `/api/payments/status/{txnId}`
- ✅ **Webhook/Callback** - Automatic notification to merchant with retry logic
- ✅ **Transaction Persistence** - MySQL database with full audit trail

### ✅ Phase 2 - Intermediate Features
- ✅ **Merchant Management** - Full CRUD for merchant accounts
- ✅ **Refund API** - Full and partial refunds with ledger updates
- ✅ **Idempotency Key Handling** - Prevents duplicate transactions
- ✅ **Transaction Ledger** - Double-entry accounting system
- ✅ **HMAC Signature Verification** - Request signing and verification

### ✅ Phase 3 - Advanced Features
- ✅ **Kafka Event-Driven Architecture** - Real-time event streaming
- ✅ **Webhook Retry & DLQ** - Exponential backoff retry mechanism
- ✅ **Payment Analytics** - Merchant-level transaction analytics
- ✅ **Fraud Detection** - High-value transaction flagging

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Kotlin 1.9.25
- **Database**: MySQL (with H2 fallback for dev)
- **Message Queue**: Apache Kafka
- **Build Tool**: Gradle
- **JDK**: Java 21

## 📋 Prerequisites

- Java 21+
- MySQL 8.0+
- Apache Kafka 3.x+ (optional, for event-driven features)
- Gradle 8.x+ (or use included wrapper)

## 🔧 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd payment-gateway
```

### 2. Configure Database

Update `src/main/resources/application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/payment_gateway?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

Or use H2 for quick testing (uncomment in application.properties):

```properties
spring.datasource.url=jdbc:h2:mem:payment_gateway
spring.h2.console.enabled=true
```

### 3. Start Kafka (Optional)

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

### 4. Build and Run

```bash
# Using Gradle wrapper
./gradlew clean build
./gradlew bootRun

# Or using IDE
# Open project in IntelliJ IDEA and run PaymentGatewayApplication
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### 🏪 Merchant Management

#### Create Merchant
```bash
POST /api/merchants
Content-Type: application/json

{
  "merchantId": "MERCHANT_001",
  "name": "Test Merchant",
  "email": "merchant@example.com",
  "callbackUrl": "http://localhost:8080/api/merchant/callback"
}

Response:
{
  "id": "uuid",
  "merchantId": "MERCHANT_001",
  "name": "Test Merchant",
  "secretKey": "generated-secret-key",
  "callbackUrl": "http://localhost:8080/api/merchant/callback",
  "balance": 0,
  "active": true,
  "createdAt": "2025-11-08T...",
  "updatedAt": "2025-11-08T..."
}
```

#### Get Merchant
```bash
GET /api/merchants/{merchantId}
```

#### Get All Merchants
```bash
GET /api/merchants
```

#### Update Callback URL
```bash
PUT /api/merchants/{merchantId}/callback
Content-Type: application/json

{
  "callbackUrl": "http://new-url.com/callback"
}
```

#### Delete Merchant
```bash
DELETE /api/merchants/{merchantId}
```

### 💳 Payment Processing

#### Initiate Payment
```bash
POST /api/payments/initiate
Content-Type: application/json
Idempotency-Key: unique-key-123 (optional)

{
  "merchantId": "MERCHANT_001",
  "orderId": "ORDER_123",
  "amount": 1000.50,
  "currency": "INR",
  "paymentMethod": "CREDIT_CARD",
  "description": "Test payment",
  "signature": "hmac-signature" (optional)
}

Response:
{
  "transactionId": "TXN_1699456789_abc123",
  "status": "INITIATED",
  "redirectUrl": "http://localhost:8080/payment-page/TXN_1699456789_abc123",
  "message": "Payment initiated successfully"
}
```

**Payment Methods**: `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `NET_BANKING`, `WALLET`

**Payment Status Flow**: 
`INITIATED` → `PROCESSING` → `SUCCESS` / `FAILED` / `REVIEW`

#### Check Payment Status
```bash
GET /api/payments/status/{transactionId}

Response:
{
  "transactionId": "TXN_1699456789_abc123",
  "merchantId": "MERCHANT_001",
  "orderId": "ORDER_123",
  "amount": 1000.50,
  "currency": "INR",
  "status": "SUCCESS",
  "paymentMethod": "CREDIT_CARD",
  "description": "Test payment",
  "refundedAmount": 0,
  "fraudulent": false,
  "fraudReason": null,
  "createdAt": "2025-11-08T...",
  "updatedAt": "2025-11-08T...",
  "completedAt": "2025-11-08T..."
}
```

#### Get Merchant Transactions
```bash
GET /api/payments/merchant/{merchantId}
```

### 🔄 Refund Management

#### Initiate Refund
```bash
POST /api/payments/refund/{transactionId}
Content-Type: application/json

{
  "amount": 500.00,
  "reason": "Customer requested refund"
}

Response:
{
  "refundId": "RFD_1699456789_xyz456",
  "transactionId": "TXN_1699456789_abc123",
  "amount": 500.00,
  "status": "PROCESSING",
  "message": "Refund initiated successfully"
}
```

#### Get Refund Status
```bash
GET /api/payments/refund/{refundId}
```

#### Get Transaction Refunds
```bash
GET /api/payments/{transactionId}/refunds
```

### 📊 Analytics

#### Get Merchant Analytics
```bash
GET /api/analytics/merchant/{merchantId}

Response:
{
  "merchantId": "MERCHANT_001",
  "totalPayments": 100,
  "successfulPayments": 90,
  "failedPayments": 10,
  "totalAmount": 100000.00,
  "successfulAmount": 95000.00,
  "totalRefunds": 5,
  "totalRefundedAmount": 2500.00,
  "successRate": 90.0
}
```

## 🔐 Security Features

### HMAC Signature Generation

```kotlin
// Generate signature for request
val dataToSign = "$merchantId:$orderId:$amount:$currency"
val signature = signatureUtil.generateSignature(dataToSign, secretKey)
```

### Idempotency

Send `Idempotency-Key` header to prevent duplicate transactions on retry.

## 🔄 Webhook/Callback Flow

1. Payment completes (SUCCESS/FAILED)
2. Gateway sends POST to merchant's `callbackUrl`
3. If webhook fails, automatic retry with exponential backoff
4. Max 5 retries with delays: 5s, 10s, 20s, 40s, 80s
5. After max retries, marked as FAILED in webhook logs

**Webhook Payload**:
```json
{
  "transactionId": "TXN_123",
  "merchantId": "MERCHANT_001",
  "orderId": "ORDER_123",
  "amount": 1000.50,
  "currency": "INR",
  "status": "SUCCESS",
  "timestamp": "2025-11-08T12:00:00"
}
```

## 📡 Kafka Events

The gateway publishes events to `payment-events` topic:

- `payment.initiated` - Payment request received
- `payment.succeeded` - Payment completed successfully
- `payment.failed` - Payment failed
- `payment.refunded` - Refund processed

## 🧪 Testing

### Quick Test Flow

1. **Create a Merchant**
```bash
curl -X POST http://localhost:8080/api/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "TEST_MERCHANT",
    "name": "Test Shop",
    "email": "test@shop.com",
    "callbackUrl": "http://localhost:8080/api/merchant/callback"
  }'
```

2. **Initiate Payment**
```bash
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "TEST_MERCHANT",
    "orderId": "ORDER_001",
    "amount": 100.00,
    "currency": "INR",
    "paymentMethod": "UPI"
  }'
```

3. **Check Status** (after 3-5 seconds)
```bash
curl http://localhost:8080/api/payments/status/TXN_xxx
```

4. **Initiate Refund**
```bash
curl -X POST http://localhost:8080/api/payments/refund/TXN_xxx \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.00,
    "reason": "Partial refund"
  }'
```

5. **View Analytics**
```bash
curl http://localhost:8080/api/analytics/merchant/TEST_MERCHANT
```

## 🎯 Fraud Detection

Transactions above ₹1,00,000 are automatically flagged for review with a 50% probability.

Status will be set to `REVIEW` instead of processing automatically.

## 📁 Project Structure

```
src/main/kotlin/com/example/payment_gateway/
├── config/              # Configuration classes
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
├── enums/               # Enums
├── event/               # Kafka Events
├── exception/           # Exception Handlers
├── repository/          # JPA Repositories
├── scheduled/           # Scheduled Tasks
├── service/             # Business Logic
└── util/                # Utility Classes
```

## 🗃️ Database Schema

### Tables
- `merchants` - Merchant accounts with wallet balance
- `transactions` - All payment transactions
- `refunds` - Refund records
- `ledger` - Double-entry accounting ledger
- `webhook_logs` - Webhook delivery logs

## 🚧 Future Enhancements

- [ ] Web UI Dashboard (React/Vue)
- [ ] Multi-currency support with exchange rates
- [ ] Payment link generation
- [ ] Recurring payments/subscriptions
- [ ] 3D Secure simulation
- [ ] Bank reconciliation
- [ ] Advanced fraud detection with ML
- [ ] Settlement reports
- [ ] Dispute management

## 📝 License

This is a educational/simulator project for learning purposes.

## 🤝 Contributing

This is a demonstration project. Feel free to fork and modify for your learning!

## 📧 Support

For issues or questions, please open an issue in the repository.

---

**Built with ❤️ using Spring Boot & Kotlin**

