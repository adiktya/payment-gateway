package com.example.payment_gateway.repository

import com.example.payment_gateway.entity.Merchant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MerchantRepository : JpaRepository<Merchant, String> {
    fun findByMerchantId(merchantId: String): Optional<Merchant>
    fun existsByMerchantId(merchantId: String): Boolean
}

