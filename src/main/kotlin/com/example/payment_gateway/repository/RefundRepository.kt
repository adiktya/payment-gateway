package com.example.payment_gateway.repository

import com.example.payment_gateway.entity.Refund
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefundRepository : JpaRepository<Refund, String> {
    fun findByRefundId(refundId: String): Optional<Refund>
    fun findByTransactionId(transactionId: String): List<Refund>
}

