package com.example.payment_gateway.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "refunds", indexes = [
    Index(name = "idx_refund_id", columnList = "refundId"),
    Index(name = "idx_transaction_id", columnList = "transactionId"),
    Index(name = "idx_merchant_id", columnList = "merchantId")
])
data class Refund(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(nullable = false, unique = true)
    val refundId: String,
    
    @Column(nullable = false)
    val transactionId: String,
    
    @Column(nullable = false)
    val merchantId: String,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "INR",
    
    @Column(length = 500)
    val reason: String? = null,
    
    @Column(nullable = false)
    var status: String = "PROCESSING", // PROCESSING, SUCCESS, FAILED
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    var completedAt: LocalDateTime? = null
)

