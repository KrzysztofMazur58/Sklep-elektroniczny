package com.example.sklepElektroniczny.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OrderDTO {

    private Long orderId;
    private String email;
    private List<OrderElementDTO> orderElements;
    private LocalDate orderDate;
    private PaymentDTO payment;
    private BigDecimal totalPrice;
    private String status;
    private Long addressId;
}
