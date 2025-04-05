package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.OrderDTO;
import com.example.sklepElektroniczny.dtos.OrderRequestDTO;
import com.example.sklepElektroniczny.service.OrderService;
import com.example.sklepElektroniczny.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final AuthUtil authUtil;

    public OrderController(OrderService orderService, AuthUtil authUtil) {
        this.orderService = orderService;
        this.authUtil = authUtil;
    }

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> order(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.getCurrentUserEmail();
        OrderDTO order = orderService.createOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPaymentGatewayName(),
                orderRequestDTO.getGatewayPaymentId(),
                orderRequestDTO.getGatewayStatus(),
                orderRequestDTO.getGatewayResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}

