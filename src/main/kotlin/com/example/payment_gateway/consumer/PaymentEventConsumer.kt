package com.example.payment_gateway.consumer

import com.example.payment_gateway.event.PaymentEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"], matchIfMissing = false)
class PaymentEventConsumer(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(PaymentEventConsumer::class.java)
    
    @KafkaListener(topics = ["payment-events"], groupId = "payment-gateway-group")
    fun consumePaymentEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, PaymentEvent::class.java)
            logger.info("Consumed payment event: ${event.eventType} for transaction: ${event.transactionId}")
            
            // Here you can add additional processing logic
            // For example: update analytics, send notifications, etc.
            
            when (event.eventType) {
                "payment.initiated" -> handlePaymentInitiated(event)
                "payment.succeeded" -> handlePaymentSucceeded(event)
                "payment.failed" -> handlePaymentFailed(event)
                "payment.refunded" -> handlePaymentRefunded(event)
                else -> logger.warn("Unknown event type: ${event.eventType}")
            }
            
        } catch (e: Exception) {
            logger.error("Error processing payment event", e)
        }
    }
    
    private fun handlePaymentInitiated(event: PaymentEvent) {
        logger.info("Processing payment initiated event: ${event.transactionId}")
        // Add custom logic here
    }
    
    private fun handlePaymentSucceeded(event: PaymentEvent) {
        logger.info("Processing payment succeeded event: ${event.transactionId}")
        // Add custom logic here (e.g., update analytics dashboard)
    }
    
    private fun handlePaymentFailed(event: PaymentEvent) {
        logger.info("Processing payment failed event: ${event.transactionId}")
        // Add custom logic here (e.g., alert merchant)
    }
    
    private fun handlePaymentRefunded(event: PaymentEvent) {
        logger.info("Processing payment refunded event: ${event.transactionId}")
        // Add custom logic here
    }
}

