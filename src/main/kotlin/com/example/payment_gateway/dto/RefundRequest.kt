package com.example.payment_gateway.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class RefundRequest(
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "Reason is required")
    val reason: String
)

