package com.microproject.rate_limiter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimitEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    public RateLimitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendRateLimitEvent(String clientId, boolean allowed) {
        String message = String.format("Client %s - Request %s", clientId, allowed ? "ALLOWED" : "DENIED");
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.info("Sent message to RabbitMQ: {}", message);
    }
}