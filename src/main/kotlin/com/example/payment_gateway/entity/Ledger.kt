package com.example.payment_gateway.entity

import com.example.payment_gateway.enums.LedgerEntryType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "ledger", indexes = [
    Index(name = "idx_transaction_id", columnList = "transactionId"),
    Index(name = "idx_merchant_id", columnList = "merchantId"),
    Index(name = "idx_entry_type", columnList = "entryType")
])
data class Ledger(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(nullable = false)
    val transactionId: String,
    
    @Column(nullable = false)
    val merchantId: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val entryType: LedgerEntryType,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "INR",
    
    @Column(nullable = false)
    val description: String,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val balanceAfter: BigDecimal,
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

