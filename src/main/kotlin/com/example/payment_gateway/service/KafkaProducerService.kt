package com.example.payment_gateway.service

import com.example.payment_gateway.event.PaymentEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"], matchIfMissing = false)
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)
    
    companion object {
        const val PAYMENT_TOPIC = "payment-events"
    }
    
    fun publishPaymentEvent(event: PaymentEvent) {
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(PAYMENT_TOPIC, event.transactionId, message)
            logger.info("Published event: ${event.eventType} for transaction: ${event.transactionId}")
        } catch (e: Exception) {
            logger.error("Failed to publish event: ${event.eventType} for transaction: ${event.transactionId}", e)
        }
    }
}

