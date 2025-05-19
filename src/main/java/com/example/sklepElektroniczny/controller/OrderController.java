package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.OrderDTO;
import com.example.sklepElektroniczny.dtos.OrderRequestDTO;
import com.example.sklepElektroniczny.dtos.StatusUpdateRequest;
import com.example.sklepElektroniczny.service.OrderService;
import com.example.sklepElektroniczny.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Pobierz zamówienia aktualnie zalogowanego użytkownika")
    @GetMapping("/user/orders")
    public ResponseEntity<?> getUserOrders() {
        String emailId = authUtil.getCurrentUserEmail();
        return ResponseEntity.ok(orderService.getOrdersByEmail(emailId));
    }

    @Operation(summary = "Pobierz wszystkie zamówienia (dla administratora)")
    @GetMapping("/admin/orders")
    public ResponseEntity<?> getAllOrders() {
        if (!authUtil.isCurrentUserAdmin() && !authUtil.isCurrentUserWorker()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brak uprawnień");
        }
        System.out.println("Użytkownik jest adminem/workiem: " + authUtil.getCurrentUserEmail());
        List<OrderDTO> allOrders = orderService.getAllOrders();
        System.out.println("Ilość zamówień do zwrócenia: " + allOrders.size());
        return ResponseEntity.ok(allOrders);
    }


    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody StatusUpdateRequest statusUpdateRequest) {
        if (!authUtil.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brak uprawnień");
        }

        if (statusUpdateRequest.getStatus() == null || statusUpdateRequest.getStatus().isEmpty()) {
            return ResponseEntity.badRequest().body("Status jest wymagany");
        }

        try {
            orderService.updateOrderStatus(orderId, statusUpdateRequest.getStatus());
            return ResponseEntity.ok("Status został zaktualizowany");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas aktualizacji statusu");
        }
    }


}

