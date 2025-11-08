package com.example.payment_gateway.service

import com.example.payment_gateway.entity.Ledger
import com.example.payment_gateway.enums.LedgerEntryType
import com.example.payment_gateway.repository.LedgerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class LedgerService(
    private val ledgerRepository: LedgerRepository,
    private val merchantService: MerchantService
) {
    private val logger = LoggerFactory.getLogger(LedgerService::class.java)
    
    @Transactional
    fun recordPaymentSuccess(transactionId: String, merchantId: String, amount: BigDecimal, currency: String) {
        // Credit merchant wallet
        val merchant = merchantService.updateMerchantBalance(merchantId, amount)
        
        val ledgerEntry = Ledger(
            transactionId = transactionId,
            merchantId = merchantId,
            entryType = LedgerEntryType.CREDIT,
            amount = amount,
            currency = currency,
            description = "Payment received",
            balanceAfter = merchant.balance
        )
        
        ledgerRepository.save(ledgerEntry)
        logger.info("Recorded payment success in ledger: $transactionId")
    }
    
    @Transactional
    fun recordRefund(transactionId: String, refundId: String, merchantId: String, amount: BigDecimal, currency: String) {
        // Debit merchant wallet
        val merchant = merchantService.updateMerchantBalance(merchantId, amount.negate())
        
        val ledgerEntry = Ledger(
            transactionId = refundId,
            merchantId = merchantId,
            entryType = LedgerEntryType.DEBIT,
            amount = amount,
            currency = currency,
            description = "Refund for transaction: $transactionId",
            balanceAfter = merchant.balance
        )
        
        ledgerRepository.save(ledgerEntry)
        logger.info("Recorded refund in ledger: $refundId for transaction: $transactionId")
    }
    
    fun getLedgerEntries(transactionId: String): List<Ledger> {
        return ledgerRepository.findByTransactionId(transactionId)
    }
    
    fun getMerchantLedger(merchantId: String): List<Ledger> {
        return ledgerRepository.findByMerchantId(merchantId)
    }
}

