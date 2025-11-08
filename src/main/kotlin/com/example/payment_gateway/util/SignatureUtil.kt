package com.example.payment_gateway.util

import org.springframework.stereotype.Component
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.*

@Component
class SignatureUtil {
    
    fun generateSignature(data: String, secretKey: String): String {
        val algorithm = "HmacSHA256"
        val mac = Mac.getInstance(algorithm)
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), algorithm)
        mac.init(secretKeySpec)
        val rawHmac = mac.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(rawHmac)
    }
    
    fun verifySignature(data: String, signature: String, secretKey: String): Boolean {
        val expectedSignature = generateSignature(data, secretKey)
        return expectedSignature == signature
    }
    
    fun generateSecretKey(): String {
        val random = UUID.randomUUID().toString().replace("-", "")
        return random
    }
}

