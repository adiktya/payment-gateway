package com.example.payment_gateway.entity

import com.example.payment_gateway.enums.PaymentMethod
import com.example.payment_gateway.enums.PaymentStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions", indexes = [
    Index(name = "idx_transaction_id", columnList = "transactionId"),
    Index(name = "idx_merchant_id", columnList = "merchantId"),
    Index(name = "idx_order_id", columnList = "orderId"),
    Index(name = "idx_idempotency_key", columnList = "idempotencyKey"),
    Index(name = "idx_status", columnList = "status")
])
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(nullable = false, unique = true)
    val transactionId: String,
    
    @Column(nullable = false)
    val merchantId: String,
    
    @Column(nullable = false)
    val orderId: String,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "INR",
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.INITIATED,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod,
    
    @Column(length = 1000)
    var description: String? = null,
    
    @Column(unique = true)
    val idempotencyKey: String? = null,
    
    @Column(length = 1000)
    var redirectUrl: String? = null,
    
    @Column(length = 500)
    var failureReason: String? = null,
    
    @Column(nullable = false, precision = 19, scale = 4)
    var refundedAmount: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val fraudCheck: Boolean = false,
    
    @Column(nullable = false)
    val fraudulent: Boolean = false,
    
    @Column(length = 500)
    var fraudReason: String? = null,
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    var completedAt: LocalDateTime? = null
)

