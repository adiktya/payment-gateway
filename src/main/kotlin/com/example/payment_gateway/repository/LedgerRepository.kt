package com.example.payment_gateway.repository

import com.example.payment_gateway.entity.Ledger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LedgerRepository : JpaRepository<Ledger, String> {
    fun findByTransactionId(transactionId: String): List<Ledger>
    fun findByMerchantId(merchantId: String): List<Ledger>
}

