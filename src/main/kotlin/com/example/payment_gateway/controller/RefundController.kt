package com.example.payment_gateway.controller

import com.example.payment_gateway.dto.RefundRequest
import com.example.payment_gateway.dto.RefundResponse
import com.example.payment_gateway.entity.Refund
import com.example.payment_gateway.service.RefundService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class RefundController(
    private val refundService: RefundService
) {
    
    @PostMapping("/refund/{transactionId}")
    fun initiateRefund(
        @PathVariable transactionId: String,
        @Valid @RequestBody request: RefundRequest
    ): ResponseEntity<RefundResponse> {
        val response = refundService.initiateRefund(transactionId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/refund/{refundId}")
    fun getRefund(@PathVariable refundId: String): ResponseEntity<RefundResponse> {
        val response = refundService.getRefund(refundId)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{transactionId}/refunds")
    fun getTransactionRefunds(@PathVariable transactionId: String): ResponseEntity<List<Refund>> {
        val refunds = refundService.getRefundsByTransaction(transactionId)
        return ResponseEntity.ok(refunds)
    }
}

