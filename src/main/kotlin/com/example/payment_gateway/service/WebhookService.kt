package com.example.payment_gateway.service

import com.example.payment_gateway.dto.WebhookPayload
import com.example.payment_gateway.entity.WebhookLog
import com.example.payment_gateway.repository.WebhookLogRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDateTime
import kotlin.math.pow

@Service
class WebhookService(
    private val webhookLogRepository: WebhookLogRepository,
    private val objectMapper: ObjectMapper,
    private val webClient: WebClient
) {
    private val logger = LoggerFactory.getLogger(WebhookService::class.java)
    
    companion object {
        const val MAX_RETRIES = 5
        const val BASE_DELAY_SECONDS = 5L
    }
    
    @Async
    @Transactional
    fun sendWebhook(payload: WebhookPayload, callbackUrl: String) {
        val payloadJson = objectMapper.writeValueAsString(payload)
        
        val webhookLog = WebhookLog(
            transactionId = payload.transactionId,
            merchantId = payload.merchantId,
            callbackUrl = callbackUrl,
            payload = payloadJson,
            status = "PENDING",
            retryCount = 0,
            maxRetries = MAX_RETRIES
        )
        
        val savedLog = webhookLogRepository.save(webhookLog)
        attemptWebhookDelivery(savedLog)
    }
    
    @Transactional
    fun attemptWebhookDelivery(webhookLog: WebhookLog) {
        logger.info("Attempting webhook delivery for transaction: ${webhookLog.transactionId}, attempt: ${webhookLog.retryCount + 1}")
        
        try {
            val response = webClient.post()
                .uri(webhookLog.callbackUrl)
                .header("Content-Type", "application/json")
                .bodyValue(webhookLog.payload)
                .retrieve()
                .toBodilessEntity()
                .block()
            
            val updatedLog = webhookLog.copy(
                status = "SUCCESS",
                responseCode = response?.statusCode?.value(),
                lastAttemptAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            webhookLogRepository.save(updatedLog)
            logger.info("Webhook delivered successfully for transaction: ${webhookLog.transactionId}")
            
        } catch (e: WebClientResponseException) {
            handleWebhookFailure(webhookLog, e.statusCode.value(), e.message)
        } catch (e: Exception) {
            handleWebhookFailure(webhookLog, null, e.message)
        }
    }
    
    @Transactional
    fun handleWebhookFailure(webhookLog: WebhookLog, statusCode: Int?, errorMessage: String?) {
        val newRetryCount = webhookLog.retryCount + 1
        
        if (newRetryCount >= webhookLog.maxRetries) {
            val updatedLog = webhookLog.copy(
                status = "FAILED",
                retryCount = newRetryCount,
                responseCode = statusCode,
                errorMessage = errorMessage,
                lastAttemptAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            webhookLogRepository.save(updatedLog)
            logger.error("Webhook failed permanently for transaction: ${webhookLog.transactionId}")
        } else {
            val delaySeconds = calculateExponentialBackoff(newRetryCount)
            val nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds)
            
            val updatedLog = webhookLog.copy(
                status = "RETRYING",
                retryCount = newRetryCount,
                responseCode = statusCode,
                errorMessage = errorMessage,
                lastAttemptAt = LocalDateTime.now(),
                nextRetryAt = nextRetryAt,
                updatedAt = LocalDateTime.now()
            )
            webhookLogRepository.save(updatedLog)
            logger.warn("Webhook failed for transaction: ${webhookLog.transactionId}, will retry at: $nextRetryAt")
        }
    }
    
    fun getWebhookLogs(transactionId: String): List<WebhookLog> {
        return webhookLogRepository.findByTransactionId(transactionId)
    }
    
    @Transactional
    fun retryFailedWebhooks() {
        val pendingWebhooks = webhookLogRepository.findPendingRetries(LocalDateTime.now())
        logger.info("Found ${pendingWebhooks.size} webhooks to retry")
        
        pendingWebhooks.forEach { webhookLog ->
            attemptWebhookDelivery(webhookLog)
        }
    }
    
    private fun calculateExponentialBackoff(retryCount: Int): Long {
        return (BASE_DELAY_SECONDS * 2.0.pow(retryCount - 1)).toLong()
    }
}

