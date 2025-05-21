package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.OrderDTO;
import com.example.sklepElektroniczny.dtos.OrderRequestDTO;
import com.example.sklepElektroniczny.dtos.StatusUpdateRequest;
import com.example.sklepElektroniczny.service.OrderService;
import com.example.sklepElektroniczny.util.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .addFilters(encodingFilter)
                .build();
    }

    @Test
    public void testOrder_Success() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        requestDTO.setAddressId(1L);
        requestDTO.setPaymentGatewayName("Stripe");
        requestDTO.setGatewayPaymentId("payment123");
        requestDTO.setGatewayStatus("SUCCESS");
        requestDTO.setGatewayResponseMessage("OK");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        orderDTO.setEmail("test@example.com");
        orderDTO.setOrderDate(LocalDate.now());
        orderDTO.setTotalPrice(BigDecimal.valueOf(100));
        orderDTO.setStatus("CREATED");

        when(authUtil.getCurrentUserEmail()).thenReturn("test@example.com");
        when(orderService.createOrder(
                eq("test@example.com"),
                eq(1L),
                eq("paypal"),
                eq("Stripe"),
                eq("payment123"),
                eq("SUCCESS"),
                eq("OK")
        )).thenReturn(orderDTO);

        mockMvc.perform(post("/api/order/users/payments/paypal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    public void testGetUserOrders_Success() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        orderDTO.setEmail("test@example.com");
        orderDTO.setStatus("CREATED");

        List<OrderDTO> orders = Collections.singletonList(orderDTO);

        when(authUtil.getCurrentUserEmail()).thenReturn("test@example.com");
        when(orderService.getOrdersByEmail("test@example.com")).thenReturn(orders);

        mockMvc.perform(get("/api/user/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    public void testGetAllOrders_AdminAccess_Success() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(1L);
        orderDTO.setStatus("CREATED");

        List<OrderDTO> orders = Collections.singletonList(orderDTO);

        when(authUtil.isCurrentUserAdmin()).thenReturn(true);
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1));
    }

    @Test
    public void testGetAllOrders_NoPermission_Forbidden() throws Exception {
        when(authUtil.isCurrentUserAdmin()).thenReturn(false);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Brak uprawnien")); // <-- usuniete polskie znaki
    }

    @Test
    public void testUpdateOrderStatus_AdminSuccess() throws Exception {
        StatusUpdateRequest statusUpdateRequest = new StatusUpdateRequest();
        statusUpdateRequest.setStatus("SHIPPED");

        when(authUtil.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Status zostal zaktualizowany")); // <-- usuniete polskie znaki
    }

    @Test
    public void testUpdateOrderStatus_MissingStatus_BadRequest() throws Exception {
        StatusUpdateRequest statusUpdateRequest = new StatusUpdateRequest();
        statusUpdateRequest.setStatus("");

        when(authUtil.isCurrentUserAdmin()).thenReturn(true);

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Status jest wymagany"));
    }

    @Test
    public void testUpdateOrderStatus_NoAdmin_Forbidden() throws Exception {
        StatusUpdateRequest statusUpdateRequest = new StatusUpdateRequest();
        statusUpdateRequest.setStatus("SHIPPED");

        when(authUtil.isCurrentUserAdmin()).thenReturn(false);

        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Brak uprawnien")); // <-- usuniete polskie znaki
    }
}

