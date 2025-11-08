package com.example.payment_gateway.dto

import com.example.payment_gateway.enums.PaymentMethod
import com.example.payment_gateway.enums.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentStatusResponse(
    val transactionId: String,
    val merchantId: String,
    val orderId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val description: String?,
    val refundedAmount: BigDecimal,
    val fraudulent: Boolean,
    val fraudReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?
)

