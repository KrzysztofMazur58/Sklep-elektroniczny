package com.example.sklepElektroniczny.rabbitmq;

import com.example.sklepElektroniczny.configuration.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("📥 Otrzymano wiadomość z kolejki: " + message);
    }
}

