package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.OrderDTO;

public interface OrderServiceInterface {
    OrderDTO createOrder(String emailId, Long addressId, String paymentMethod, String paymentGatewayName, String gatewayPaymentId, String gatewayStatus, String gatewayResponseMessage);
}
