package com.example.payment_gateway.service

import com.example.payment_gateway.dto.RefundRequest
import com.example.payment_gateway.dto.RefundResponse
import com.example.payment_gateway.entity.Refund
import com.example.payment_gateway.enums.PaymentStatus
import com.example.payment_gateway.event.PaymentEvent
import com.example.payment_gateway.repository.RefundRepository
import com.example.payment_gateway.repository.TransactionRepository
import com.example.payment_gateway.util.IdGenerator
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class RefundService(
    private val refundRepository: RefundRepository,
    private val transactionRepository: TransactionRepository,
    private val ledgerService: LedgerService,
    private val kafkaProducerService: KafkaProducerService?,
    private val idGenerator: IdGenerator
) {
    private val logger = LoggerFactory.getLogger(RefundService::class.java)
    
    @Transactional
    fun initiateRefund(transactionId: String, request: RefundRequest): RefundResponse {
        val transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow { throw IllegalArgumentException("Transaction not found: $transactionId") }
        
        // Validate transaction status
        if (transaction.status != PaymentStatus.SUCCESS && transaction.status != PaymentStatus.PARTIAL_REFUNDED) {
            throw IllegalStateException("Cannot refund transaction with status: ${transaction.status}")
        }
        
        // Calculate available refund amount
        val availableAmount = transaction.amount.subtract(transaction.refundedAmount)
        if (request.amount > availableAmount) {
            throw IllegalArgumentException("Refund amount exceeds available amount. Available: $availableAmount")
        }
        
        val refundId = idGenerator.generateRefundId()
        val refund = Refund(
            refundId = refundId,
            transactionId = transactionId,
            merchantId = transaction.merchantId,
            amount = request.amount,
            currency = transaction.currency,
            reason = request.reason,
            status = "PROCESSING"
        )
        
        val savedRefund = refundRepository.save(refund)
        logger.info("Refund initiated: $refundId for transaction: $transactionId")
        
        // Process refund asynchronously
        processRefundAsync(refundId)
        
        return RefundResponse(
            refundId = savedRefund.refundId,
            transactionId = savedRefund.transactionId,
            amount = savedRefund.amount,
            status = savedRefund.status,
            message = "Refund initiated successfully"
        )
    }
    
    @Async
    fun processRefundAsync(refundId: String) {
        try {
            // Simulate processing delay
            Thread.sleep(2000)
            
            // Simulate success (95% success rate)
            val success = Random.nextInt(100) < 95
            
            if (success) {
                completeRefundSuccess(refundId)
            } else {
                completeRefundFailure(refundId, "Bank declined refund")
            }
            
        } catch (e: Exception) {
            logger.error("Error processing refund: $refundId", e)
            completeRefundFailure(refundId, "Internal processing error")
        }
    }
    
    @Transactional
    fun completeRefundSuccess(refundId: String) {
        val refund = refundRepository.findByRefundId(refundId)
            .orElseThrow { throw IllegalArgumentException("Refund not found: $refundId") }
        
        val transaction = transactionRepository.findByTransactionId(refund.transactionId)
            .orElseThrow { throw IllegalArgumentException("Transaction not found: ${refund.transactionId}") }
        
        // Update refund status
        val updatedRefund = refund.copy(
            status = "SUCCESS",
            completedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        refundRepository.save(updatedRefund)
        
        // Update transaction
        val newRefundedAmount = transaction.refundedAmount.add(refund.amount)
        val newStatus = if (newRefundedAmount >= transaction.amount) {
            PaymentStatus.REFUNDED
        } else {
            PaymentStatus.PARTIAL_REFUNDED
        }
        
        val updatedTransaction = transaction.copy(
            refundedAmount = newRefundedAmount,
            status = newStatus,
            updatedAt = LocalDateTime.now()
        )
        transactionRepository.save(updatedTransaction)
        
        logger.info("Refund completed successfully: $refundId")
        
        // Record in ledger
        ledgerService.recordRefund(
            transactionId = transaction.transactionId,
            refundId = refundId,
            merchantId = transaction.merchantId,
            amount = refund.amount,
            currency = refund.currency
        )
        
        // Publish event (if Kafka is available)
        val event = PaymentEvent(
            eventType = "payment.refunded",
            transactionId = transaction.transactionId,
            merchantId = transaction.merchantId,
            orderId = transaction.orderId,
            amount = refund.amount,
            currency = refund.currency,
            status = newStatus
        )
        kafkaProducerService?.publishPaymentEvent(event)
    }
    
    @Transactional
    fun completeRefundFailure(refundId: String, reason: String) {
        val refund = refundRepository.findByRefundId(refundId)
            .orElseThrow { throw IllegalArgumentException("Refund not found: $refundId") }
        
        val updatedRefund = refund.copy(
            status = "FAILED",
            reason = reason,
            completedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        refundRepository.save(updatedRefund)
        
        logger.info("Refund failed: $refundId, reason: $reason")
    }
    
    fun getRefund(refundId: String): RefundResponse {
        val refund = refundRepository.findByRefundId(refundId)
            .orElseThrow { throw IllegalArgumentException("Refund not found: $refundId") }
        
        return RefundResponse(
            refundId = refund.refundId,
            transactionId = refund.transactionId,
            amount = refund.amount,
            status = refund.status,
            message = refund.reason ?: "Refund processed"
        )
    }
    
    fun getRefundsByTransaction(transactionId: String): List<Refund> {
        return refundRepository.findByTransactionId(transactionId)
    }
}

