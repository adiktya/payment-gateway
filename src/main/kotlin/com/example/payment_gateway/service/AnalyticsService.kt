package com.example.payment_gateway.service

import com.example.payment_gateway.dto.AnalyticsResponse
import com.example.payment_gateway.enums.PaymentStatus
import com.example.payment_gateway.repository.RefundRepository
import com.example.payment_gateway.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class AnalyticsService(
    private val transactionRepository: TransactionRepository,
    private val refundRepository: RefundRepository
) {
    
    fun getMerchantAnalytics(merchantId: String): AnalyticsResponse {
        val allTransactions = transactionRepository.findByMerchantId(merchantId)
        
        val totalPayments = allTransactions.size.toLong()
        val successfulPayments = transactionRepository.countByMerchantIdAndStatus(merchantId, PaymentStatus.SUCCESS)
        val failedPayments = transactionRepository.countByMerchantIdAndStatus(merchantId, PaymentStatus.FAILED)
        
        val totalAmount = allTransactions.sumOf { it.amount }
        val successfulAmount = transactionRepository.sumAmountByMerchantIdAndStatus(merchantId, PaymentStatus.SUCCESS) ?: BigDecimal.ZERO
        
        val allRefunds = allTransactions.flatMap { refundRepository.findByTransactionId(it.transactionId) }
        val totalRefunds = allRefunds.size.toLong()
        val totalRefundedAmount = allRefunds.filter { it.status == "SUCCESS" }.sumOf { it.amount }
        
        val successRate = if (totalPayments > 0) {
            (successfulPayments.toDouble() / totalPayments.toDouble()) * 100.0
        } else {
            0.0
        }
        
        return AnalyticsResponse(
            merchantId = merchantId,
            totalPayments = totalPayments,
            successfulPayments = successfulPayments,
            failedPayments = failedPayments,
            totalAmount = totalAmount,
            successfulAmount = successfulAmount,
            totalRefunds = totalRefunds,
            totalRefundedAmount = totalRefundedAmount,
            successRate = BigDecimal(successRate).setScale(2, RoundingMode.HALF_UP).toDouble()
        )
    }
}

