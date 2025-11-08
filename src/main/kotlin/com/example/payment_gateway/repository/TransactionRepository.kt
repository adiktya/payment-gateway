package com.example.payment_gateway.repository

import com.example.payment_gateway.entity.Transaction
import com.example.payment_gateway.enums.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TransactionRepository : JpaRepository<Transaction, String> {
    fun findByTransactionId(transactionId: String): Optional<Transaction>
    fun findByIdempotencyKey(idempotencyKey: String): Optional<Transaction>
    fun findByMerchantId(merchantId: String): List<Transaction>
    fun findByMerchantIdAndStatus(merchantId: String, status: PaymentStatus): List<Transaction>
    
    @Query("SELECT t FROM Transaction t WHERE t.merchantId = :merchantId")
    fun findAllByMerchantId(merchantId: String): List<Transaction>
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.merchantId = :merchantId AND t.status = :status")
    fun countByMerchantIdAndStatus(merchantId: String, status: PaymentStatus): Long
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.merchantId = :merchantId AND t.status = :status")
    fun sumAmountByMerchantIdAndStatus(merchantId: String, status: PaymentStatus): java.math.BigDecimal?
}

