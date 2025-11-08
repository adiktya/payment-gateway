package com.example.payment_gateway.repository

import com.example.payment_gateway.entity.WebhookLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface WebhookLogRepository : JpaRepository<WebhookLog, String> {
    fun findByTransactionId(transactionId: String): List<WebhookLog>
    
    @Query("SELECT w FROM WebhookLog w WHERE w.status IN ('PENDING', 'RETRYING') AND w.nextRetryAt <= :now AND w.retryCount < w.maxRetries")
    fun findPendingRetries(now: LocalDateTime): List<WebhookLog>
}

