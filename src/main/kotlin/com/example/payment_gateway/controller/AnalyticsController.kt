package com.example.payment_gateway.controller

import com.example.payment_gateway.dto.AnalyticsResponse
import com.example.payment_gateway.service.AnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {
    
    @GetMapping("/merchant/{merchantId}")
    fun getMerchantAnalytics(@PathVariable merchantId: String): ResponseEntity<AnalyticsResponse> {
        val analytics = analyticsService.getMerchantAnalytics(merchantId)
        return ResponseEntity.ok(analytics)
    }
}

