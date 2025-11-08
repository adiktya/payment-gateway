package com.example.payment_gateway.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class MerchantResponse(
    val id: String?,
    val merchantId: String,
    val name: String,
    val email: String,
    val secretKey: String,
    val callbackUrl: String,
    val balance: BigDecimal,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

