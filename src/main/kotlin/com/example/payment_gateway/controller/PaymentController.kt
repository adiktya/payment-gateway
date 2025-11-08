package com.example.payment_gateway.controller

import com.example.payment_gateway.dto.PaymentInitiateRequest
import com.example.payment_gateway.dto.PaymentInitiateResponse
import com.example.payment_gateway.dto.PaymentStatusResponse
import com.example.payment_gateway.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    
    @PostMapping("/initiate")
    fun initiatePayment(
        @Valid @RequestBody request: PaymentInitiateRequest,
        @RequestHeader("Idempotency-Key", required = false) idempotencyKey: String?
    ): ResponseEntity<PaymentInitiateResponse> {
        val requestWithIdempotency = if (idempotencyKey != null && request.idempotencyKey == null) {
            request.copy(idempotencyKey = idempotencyKey)
        } else {
            request
        }
        
        val response = paymentService.initiatePayment(requestWithIdempotency)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/status/{transactionId}")
    fun getPaymentStatus(@PathVariable transactionId: String): ResponseEntity<PaymentStatusResponse> {
        val response = paymentService.getPaymentStatus(transactionId)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/merchant/{merchantId}")
    fun getMerchantTransactions(@PathVariable merchantId: String): ResponseEntity<List<PaymentStatusResponse>> {
        val transactions = paymentService.getTransactionsByMerchant(merchantId)
        return ResponseEntity.ok(transactions)
    }
}

