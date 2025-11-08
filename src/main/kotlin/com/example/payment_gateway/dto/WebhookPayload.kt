package com.example.payment_gateway.dto

import com.example.payment_gateway.enums.PaymentStatus
import java.math.BigDecimal

data class WebhookPayload(
    val transactionId: String,
    val merchantId: String,
    val orderId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val timestamp: String
)

