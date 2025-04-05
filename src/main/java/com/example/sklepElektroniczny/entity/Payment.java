package com.example.sklepElektroniczny.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Order order;

    @NotBlank
    @Size(min = 3, message = "Payment method must contain at least 3 characters")
    private String method;

    private String gatewayPaymentId;
    private String gatewayStatus;
    private String gatewayResponseMessage;
    private String paymentGatewayName;

    public Payment(String method, String gatewayPaymentId, String gatewayStatus, String gatewayResponseMessage, String paymentGatewayName) {
        this.method = method;
        this.gatewayPaymentId = gatewayPaymentId;
        this.gatewayStatus = gatewayStatus;
        this.gatewayResponseMessage = gatewayResponseMessage;
        this.paymentGatewayName = paymentGatewayName;
    }
}

