package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartServiceInterface {

    CartDTO addItemToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Long cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, int quantity);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);
}
