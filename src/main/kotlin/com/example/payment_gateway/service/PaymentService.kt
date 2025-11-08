package com.example.payment_gateway.service

import com.example.payment_gateway.dto.*
import com.example.payment_gateway.entity.Transaction
import com.example.payment_gateway.enums.PaymentStatus
import com.example.payment_gateway.event.PaymentEvent
import com.example.payment_gateway.repository.TransactionRepository
import com.example.payment_gateway.util.IdGenerator
import com.example.payment_gateway.util.SignatureUtil
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class PaymentService(
    private val transactionRepository: TransactionRepository,
    private val merchantService: MerchantService,
    private val ledgerService: LedgerService,
    private val webhookService: WebhookService,
    private val kafkaProducerService: KafkaProducerService?,
    private val signatureUtil: SignatureUtil,
    private val idGenerator: IdGenerator
) {
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)
    
    companion object {
        const val FRAUD_THRESHOLD = 100000.0 // ₹1,00,000
        const val MIN_PROCESSING_TIME_MS = 2000L
        const val MAX_PROCESSING_TIME_MS = 5000L
        const val PAYMENT_PAGE_URL = "http://localhost:8080/payment-page"
    }
    
    @Transactional
    fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        // Verify merchant exists
        val merchant = merchantService.getMerchantEntity(request.merchantId)
        
        // Verify signature if provided
        if (request.signature != null) {
            val dataToSign = "${request.merchantId}:${request.orderId}:${request.amount}:${request.currency}"
            if (!signatureUtil.verifySignature(dataToSign, request.signature, merchant.secretKey)) {
                throw SecurityException("Invalid signature")
            }
        }
        
        // Handle idempotency
        if (request.idempotencyKey != null) {
            val existingTransaction = transactionRepository.findByIdempotencyKey(request.idempotencyKey)
            if (existingTransaction.isPresent) {
                logger.info("Idempotent request detected, returning existing transaction")
                val txn = existingTransaction.get()
                return PaymentInitiateResponse(
                    transactionId = txn.transactionId,
                    status = txn.status,
                    redirectUrl = txn.redirectUrl ?: "$PAYMENT_PAGE_URL/${txn.transactionId}",
                    message = "Transaction already exists"
                )
            }
        }
        
        // Check for fraud
        val fraudCheck = request.amount.toDouble() > FRAUD_THRESHOLD
        val fraudulent = fraudCheck && Random.nextBoolean() // 50% chance if above threshold
        
        val transactionId = idGenerator.generateTransactionId()
        val redirectUrl = "$PAYMENT_PAGE_URL/$transactionId"
        
        val transaction = Transaction(
            transactionId = transactionId,
            merchantId = request.merchantId,
            orderId = request.orderId,
            amount = request.amount,
            currency = request.currency,
            status = if (fraudulent) PaymentStatus.REVIEW else PaymentStatus.INITIATED,
            paymentMethod = request.paymentMethod,
            description = request.description,
            idempotencyKey = request.idempotencyKey,
            redirectUrl = redirectUrl,
            fraudCheck = fraudCheck,
            fraudulent = fraudulent,
            fraudReason = if (fraudulent) "High value transaction flagged" else null
        )
        
        val savedTransaction = transactionRepository.save(transaction)
        logger.info("Payment initiated: ${savedTransaction.transactionId}")
        
        // Publish event (if Kafka is available)
        val event = PaymentEvent(
            eventType = "payment.initiated",
            transactionId = savedTransaction.transactionId,
            merchantId = savedTransaction.merchantId,
            orderId = savedTransaction.orderId,
            amount = savedTransaction.amount,
            currency = savedTransaction.currency,
            status = savedTransaction.status
        )
        kafkaProducerService?.publishPaymentEvent(event)
        
        // Start async processing if not fraudulent
        if (!fraudulent) {
            processPaymentAsync(transactionId)
        }
        
        return PaymentInitiateResponse(
            transactionId = savedTransaction.transactionId,
            status = savedTransaction.status,
            redirectUrl = redirectUrl,
            message = if (fraudulent) "Transaction under review" else "Payment initiated successfully"
        )
    }
    
    @Async
    fun processPaymentAsync(transactionId: String) {
        try {
            // Simulate processing delay
            val delay = Random.nextLong(MIN_PROCESSING_TIME_MS, MAX_PROCESSING_TIME_MS)
            Thread.sleep(delay)
            
            // Update to PROCESSING
            updateTransactionStatus(transactionId, PaymentStatus.PROCESSING)
            
            // Simulate another delay
            Thread.sleep(1000)
            
            // Simulate success/failure (90% success rate)
            val success = Random.nextInt(100) < 90
            
            if (success) {
                completePaymentSuccess(transactionId)
            } else {
                completePaymentFailure(transactionId, "Insufficient funds")
            }
            
        } catch (e: Exception) {
            logger.error("Error processing payment: $transactionId", e)
            completePaymentFailure(transactionId, "Internal processing error")
        }
    }
    
    @Transactional
    fun updateTransactionStatus(transactionId: String, status: PaymentStatus) {
        val transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow { throw IllegalArgumentException("Transaction not found: $transactionId") }
        
        val updatedTransaction = transaction.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        
        transactionRepository.save(updatedTransaction)
        logger.info("Transaction $transactionId status updated to $status")
    }
    
    @Transactional
    fun completePaymentSuccess(transactionId: String) {
        val transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow { throw IllegalArgumentException("Transaction not found: $transactionId") }
        
        val updatedTransaction = transaction.copy(
            status = PaymentStatus.SUCCESS,
            completedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val saved = transactionRepository.save(updatedTransaction)
        logger.info("Payment completed successfully: $transactionId")
        
        // Record in ledger
        ledgerService.recordPaymentSuccess(
            transactionId = transactionId,
            merchantId = saved.merchantId,
            amount = saved.amount,
            currency = saved.currency
        )
        
        // Publish success event (if Kafka is available)
        val event = PaymentEvent(
            eventType = "payment.succeeded",
            transactionId = saved.transactionId,
            merchantId = saved.merchantId,
            orderId = saved.orderId,
            amount = saved.amount,
            currency = saved.currency,
            status = PaymentStatus.SUCCESS
        )
        kafkaProducerService?.publishPaymentEvent(event)
        
        // Send webhook
        val merchant = merchantService.getMerchantEntity(saved.merchantId)
        val webhookPayload = WebhookPayload(
            transactionId = saved.transactionId,
            merchantId = saved.merchantId,
            orderId = saved.orderId,
            amount = saved.amount,
            currency = saved.currency,
            status = PaymentStatus.SUCCESS,
            timestamp = saved.completedAt.toString()
        )
        webhookService.sendWebhook(webhookPayload, merchant.callbackUrl)
    }
    
    @Transactional
    fun completePaymentFailure(transactionId: String, reason: String) {
        val transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow { throw IllegalArgumentException("Transaction not found: $transactionId") }
        
        val updatedTransaction = transaction.copy(
            status = PaymentStatus.FAILED,
            failureReason = reason,
            completedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val saved = transactionRepository.save(updatedTransaction)
        logger.info("Payment failed: $transactionId, reason: $reason")
        
        // Publish failure event (if Kafka is available)
        val event = PaymentEvent(
            eventType = "payment.failed",
            transactionId = saved.transactionId,
            merchantId = saved.merchantId,
            orderId = saved.orderId,
            amount = saved.amount,
            currency = saved.currency,
            status = PaymentStatus.FAILED
        )
        kafkaProducerService?.publishPaymentEvent(event)
        
        // Send webhook
        val merchant = merchantService.getMerchantEntity(saved.merchantId)
        val webhookPayload = WebhookPayload(
            transactionId = saved.transactionId,
            merchantId = saved.merchantId,
            orderId = saved.orderId,
            amount = saved.amount,
            currency = saved.currency,
            status = PaymentStatus.FAILED,
            timestamp = saved.completedAt.toString()
        )
        webhookService.sendWebhook(webhookPayload, merchant.callbackUrl)
    }
    
    fun getPaymentStatus(transactionId: String): PaymentStatusResponse {
        val transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow { throw IllegalArgumentException("Transaction not found: $transactionId") }
        
        return PaymentStatusResponse(
            transactionId = transaction.transactionId,
            merchantId = transaction.merchantId,
            orderId = transaction.orderId,
            amount = transaction.amount,
            currency = transaction.currency,
            status = transaction.status,
            paymentMethod = transaction.paymentMethod,
            description = transaction.description,
            refundedAmount = transaction.refundedAmount,
            fraudulent = transaction.fraudulent,
            fraudReason = transaction.fraudReason,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt,
            completedAt = transaction.completedAt
        )
    }
    
    fun getTransactionsByMerchant(merchantId: String): List<PaymentStatusResponse> {
        return transactionRepository.findByMerchantId(merchantId).map { transaction ->
            PaymentStatusResponse(
                transactionId = transaction.transactionId,
                merchantId = transaction.merchantId,
                orderId = transaction.orderId,
                amount = transaction.amount,
                currency = transaction.currency,
                status = transaction.status,
                paymentMethod = transaction.paymentMethod,
                description = transaction.description,
                refundedAmount = transaction.refundedAmount,
                fraudulent = transaction.fraudulent,
                fraudReason = transaction.fraudReason,
                createdAt = transaction.createdAt,
                updatedAt = transaction.updatedAt,
                completedAt = transaction.completedAt
            )
        }
    }
}

