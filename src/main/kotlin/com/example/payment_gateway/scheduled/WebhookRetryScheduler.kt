package com.example.payment_gateway.scheduled

import com.example.payment_gateway.service.WebhookService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WebhookRetryScheduler(
    private val webhookService: WebhookService
) {
    private val logger = LoggerFactory.getLogger(WebhookRetryScheduler::class.java)
    
    // Run every 30 seconds
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    fun retryFailedWebhooks() {
        logger.debug("Running webhook retry scheduler")
        webhookService.retryFailedWebhooks()
    }
}

