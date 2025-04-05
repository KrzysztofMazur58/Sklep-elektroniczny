package com.example.sklepElektroniczny.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OrderRequestDTO {

    private Long addressId;
    private String method;
    private String gatewayPaymentId;
    private String gatewayStatus;
    private String gatewayResponseMessage;
    private String paymentGatewayName;
}
