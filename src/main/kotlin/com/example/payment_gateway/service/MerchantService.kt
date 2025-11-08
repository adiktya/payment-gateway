package com.example.payment_gateway.service

import com.example.payment_gateway.dto.MerchantCreateRequest
import com.example.payment_gateway.dto.MerchantResponse
import com.example.payment_gateway.entity.Merchant
import com.example.payment_gateway.repository.MerchantRepository
import com.example.payment_gateway.util.SignatureUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class MerchantService(
    private val merchantRepository: MerchantRepository,
    private val signatureUtil: SignatureUtil
) {
    private val logger = LoggerFactory.getLogger(MerchantService::class.java)
    
    @Transactional
    fun createMerchant(request: MerchantCreateRequest): MerchantResponse {
        if (merchantRepository.existsByMerchantId(request.merchantId)) {
            throw IllegalArgumentException("Merchant with ID ${request.merchantId} already exists")
        }
        
        val secretKey = signatureUtil.generateSecretKey()
        val merchant = Merchant(
            merchantId = request.merchantId,
            name = request.name,
            email = request.email,
            secretKey = secretKey,
            callbackUrl = request.callbackUrl,
            balance = BigDecimal.ZERO,
            active = true
        )
        
        val savedMerchant = merchantRepository.save(merchant)
        logger.info("Created merchant: ${savedMerchant.merchantId}")
        
        return toResponse(savedMerchant)
    }
    
    fun getMerchant(merchantId: String): MerchantResponse {
        val merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow { throw IllegalArgumentException("Merchant not found: $merchantId") }
        return toResponse(merchant)
    }
    
    fun getAllMerchants(): List<MerchantResponse> {
        return merchantRepository.findAll().map { toResponse(it) }
    }
    
    @Transactional
    fun updateMerchantBalance(merchantId: String, amount: BigDecimal): Merchant {
        val merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow { throw IllegalArgumentException("Merchant not found: $merchantId") }
        
        val updatedMerchant = merchant.copy(
            balance = merchant.balance.add(amount),
            updatedAt = LocalDateTime.now()
        )
        
        return merchantRepository.save(updatedMerchant)
    }
    
    @Transactional
    fun updateCallbackUrl(merchantId: String, callbackUrl: String): MerchantResponse {
        val merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow { throw IllegalArgumentException("Merchant not found: $merchantId") }
        
        val updatedMerchant = merchant.copy(
            callbackUrl = callbackUrl,
            updatedAt = LocalDateTime.now()
        )
        
        val saved = merchantRepository.save(updatedMerchant)
        logger.info("Updated callback URL for merchant: $merchantId")
        
        return toResponse(saved)
    }
    
    @Transactional
    fun deleteMerchant(merchantId: String) {
        val merchant = merchantRepository.findByMerchantId(merchantId)
            .orElseThrow { throw IllegalArgumentException("Merchant not found: $merchantId") }
        
        merchantRepository.delete(merchant)
        logger.info("Deleted merchant: $merchantId")
    }
    
    fun getMerchantEntity(merchantId: String): Merchant {
        return merchantRepository.findByMerchantId(merchantId)
            .orElseThrow { throw IllegalArgumentException("Merchant not found: $merchantId") }
    }
    
    private fun toResponse(merchant: Merchant): MerchantResponse {
        return MerchantResponse(
            id = merchant.id,
            merchantId = merchant.merchantId,
            name = merchant.name,
            email = merchant.email,
            secretKey = merchant.secretKey,
            callbackUrl = merchant.callbackUrl,
            balance = merchant.balance,
            active = merchant.active,
            createdAt = merchant.createdAt,
            updatedAt = merchant.updatedAt
        )
    }
}

