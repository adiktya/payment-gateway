package com.example.payment_gateway.dto

import java.math.BigDecimal

data class RefundResponse(
    val refundId: String,
    val transactionId: String,
    val amount: BigDecimal,
    val status: String,
    val message: String
)

