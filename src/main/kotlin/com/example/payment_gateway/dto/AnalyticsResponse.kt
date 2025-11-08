package com.example.payment_gateway.dto

import java.math.BigDecimal

data class AnalyticsResponse(
    val merchantId: String,
    val totalPayments: Long,
    val successfulPayments: Long,
    val failedPayments: Long,
    val totalAmount: BigDecimal,
    val successfulAmount: BigDecimal,
    val totalRefunds: Long,
    val totalRefundedAmount: BigDecimal,
    val successRate: Double
)

