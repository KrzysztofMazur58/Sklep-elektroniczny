package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.CartDTO;
import com.example.sklepElektroniczny.entity.Cart;
import com.example.sklepElektroniczny.repository.CartRepository;
import com.example.sklepElektroniczny.service.CartService;
import com.example.sklepElektroniczny.util.AuthUtil;
import com.example.sklepElektroniczny.exceptions.MyGlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cartController)
                .setControllerAdvice(new MyGlobalExceptionHandler())
                .build();
    }

    @Test
    public void testAddItemToCart_Success() throws Exception {
        CartDTO response = new CartDTO();
        response.setCartId(1L);

        when(cartService.addItemToCart(1L, 2)).thenReturn(response);

        mockMvc.perform(post("/api/carts/products/1/quantity/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartId").value(1));
    }

    @Test
    public void testGetCarts_Success() throws Exception {
        CartDTO dto = new CartDTO();
        dto.setCartId(1L);
        List<CartDTO> list = Collections.singletonList(dto);

        when(cartService.getAllCarts()).thenReturn(list);

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].cartId").value(1));
    }

    @Test
    public void testGetCartById_Success() throws Exception {
        String email = "user@example.com";
        Cart cart = new Cart();
        cart.setCartId(5L);

        CartDTO dto = new CartDTO();
        dto.setCartId(5L);

        when(authUtil.getCurrentUserEmail()).thenReturn(email);
        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(cartService.getCart(eq(email), eq(5L))).thenReturn(dto);

        mockMvc.perform(get("/api/carts/users/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(5));
    }

    @Test
    public void testUpdateCartProduct_Success() throws Exception {
        CartDTO dto = new CartDTO();
        dto.setCartId(7L);

        when(cartService.updateProductQuantityInCart(3L, 1)).thenReturn(dto);

        mockMvc.perform(put("/api/cart/products/3/quantity/increase"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(7));
    }

    @Test
    public void testDeleteProductFromCart_Success() throws Exception {
        when(cartService.deleteProductFromCart(5L, 10L)).thenReturn("Deleted");

        mockMvc.perform(delete("/api/carts/5/product/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }
}


