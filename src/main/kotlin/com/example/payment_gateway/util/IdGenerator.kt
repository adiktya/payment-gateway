package com.example.payment_gateway.util

import org.springframework.stereotype.Component
import java.util.*

@Component
class IdGenerator {
    
    fun generateTransactionId(): String {
        return "TXN_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
    
    fun generateRefundId(): String {
        return "RFD_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
    
    fun generateMerchantId(): String {
        return "MER_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}

