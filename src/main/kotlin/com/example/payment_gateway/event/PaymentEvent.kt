package com.example.payment_gateway.event

import com.example.payment_gateway.enums.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentEvent(
    val eventType: String, // payment.initiated, payment.succeeded, payment.failed, payment.refunded
    val transactionId: String,
    val merchantId: String,
    val orderId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

