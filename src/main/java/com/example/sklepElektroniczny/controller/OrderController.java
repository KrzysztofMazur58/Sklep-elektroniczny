package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.OrderDTO;
import com.example.sklepElektroniczny.dtos.OrderRequestDTO;
import com.example.sklepElektroniczny.service.OrderService;
import com.example.sklepElektroniczny.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Złóż zamówienie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zamówienie zostało utworzone pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> order(@Parameter(description = "Metoda płatności", example = "paypal") @PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
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

