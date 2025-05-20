package com.example.sklepElektroniczny.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "productQueue";

    @Bean
    public Queue productQueue() {
        return new Queue(QUEUE_NAME, false);
    }
}

