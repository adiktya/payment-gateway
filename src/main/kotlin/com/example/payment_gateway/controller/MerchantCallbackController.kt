package com.example.payment_gateway.controller

import com.example.payment_gateway.dto.WebhookPayload
import com.example.payment_gateway.enums.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/merchant")
class MerchantCallbackController {
    
    private val logger = LoggerFactory.getLogger(MerchantCallbackController::class.java)
    
    @PostMapping("/callback")
    fun handleCallback(@RequestBody payload: WebhookPayload): ResponseEntity<Map<String, String>> {
        logger.info("Received webhook callback for transaction: ${payload.transactionId}, status: ${payload.status}")
        
        // This is a mock endpoint that merchants would implement
        // In a real scenario, merchants would have their own endpoints
        
        return ResponseEntity.ok(mapOf(
            "status" to "received",
            "transactionId" to payload.transactionId
        ))
    }
}

