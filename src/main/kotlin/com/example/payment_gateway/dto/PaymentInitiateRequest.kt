package com.example.payment_gateway.dto

import com.example.payment_gateway.enums.PaymentMethod
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class PaymentInitiateRequest(
    @field:NotBlank(message = "Merchant ID is required")
    val merchantId: String,
    
    @field:NotBlank(message = "Order ID is required")
    val orderId: String,
    
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,
    
    val currency: String = "INR",
    
    val paymentMethod: PaymentMethod,
    
    val description: String? = null,
    
    val idempotencyKey: String? = null,
    
    val signature: String? = null
)

