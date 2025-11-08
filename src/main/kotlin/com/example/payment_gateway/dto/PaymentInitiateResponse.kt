package com.example.payment_gateway.dto

import com.example.payment_gateway.enums.PaymentStatus

data class PaymentInitiateResponse(
    val transactionId: String,
    val status: PaymentStatus,
    val redirectUrl: String,
    val message: String = "Payment initiated successfully"
)

