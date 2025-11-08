package com.example.payment_gateway.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"], matchIfMissing = false)
class KafkaConfig {
    
    @Bean
    fun paymentEventsTopic(): NewTopic {
        return TopicBuilder.name("payment-events")
            .partitions(3)
            .replicas(1)
            .build()
    }
}

