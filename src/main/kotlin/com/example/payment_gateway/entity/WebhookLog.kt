package com.example.payment_gateway.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "webhook_logs", indexes = [
    Index(name = "idx_transaction_id", columnList = "transactionId"),
    Index(name = "idx_merchant_id", columnList = "merchantId"),
    Index(name = "idx_status", columnList = "status")
])
data class WebhookLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(nullable = false)
    val transactionId: String,
    
    @Column(nullable = false)
    val merchantId: String,
    
    @Column(nullable = false, length = 1000)
    val callbackUrl: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,
    
    @Column(nullable = false)
    var status: String = "PENDING", // PENDING, SUCCESS, FAILED, RETRYING
    
    @Column(nullable = false)
    var retryCount: Int = 0,
    
    @Column(nullable = false)
    var maxRetries: Int = 5,
    
    @Column(length = 1000)
    var responseBody: String? = null,
    
    @Column
    var responseCode: Int? = null,
    
    @Column(length = 1000)
    var errorMessage: String? = null,
    
    var lastAttemptAt: LocalDateTime? = null,
    
    var nextRetryAt: LocalDateTime? = null,
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

