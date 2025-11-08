package com.example.payment_gateway.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MerchantCreateRequest(
    @field:NotBlank(message = "Merchant ID is required")
    val merchantId: String,
    
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Callback URL is required")
    val callbackUrl: String
)

