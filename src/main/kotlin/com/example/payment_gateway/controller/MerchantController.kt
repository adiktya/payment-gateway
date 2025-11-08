package com.example.payment_gateway.controller

import com.example.payment_gateway.dto.MerchantCreateRequest
import com.example.payment_gateway.dto.MerchantResponse
import com.example.payment_gateway.service.MerchantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/merchants")
class MerchantController(
    private val merchantService: MerchantService
) {
    
    @PostMapping
    fun createMerchant(@Valid @RequestBody request: MerchantCreateRequest): ResponseEntity<MerchantResponse> {
        val response = merchantService.createMerchant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{merchantId}")
    fun getMerchant(@PathVariable merchantId: String): ResponseEntity<MerchantResponse> {
        val response = merchantService.getMerchant(merchantId)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping
    fun getAllMerchants(): ResponseEntity<List<MerchantResponse>> {
        val merchants = merchantService.getAllMerchants()
        return ResponseEntity.ok(merchants)
    }
    
    @PutMapping("/{merchantId}/callback")
    fun updateCallbackUrl(
        @PathVariable merchantId: String,
        @RequestBody callbackUrl: Map<String, String>
    ): ResponseEntity<MerchantResponse> {
        val url = callbackUrl["callbackUrl"] 
            ?: throw IllegalArgumentException("callbackUrl is required")
        val response = merchantService.updateCallbackUrl(merchantId, url)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/{merchantId}")
    fun deleteMerchant(@PathVariable merchantId: String): ResponseEntity<Void> {
        merchantService.deleteMerchant(merchantId)
        return ResponseEntity.noContent().build()
    }
}

