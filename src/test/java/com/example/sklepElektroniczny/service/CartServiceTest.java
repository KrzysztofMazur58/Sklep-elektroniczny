package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.CartDTO;
import com.example.sklepElektroniczny.entity.Cart;
import com.example.sklepElektroniczny.entity.CartElement;
import com.example.sklepElektroniczny.entity.Product;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.CartElementRepository;
import com.example.sklepElektroniczny.repository.CartRepository;
import com.example.sklepElektroniczny.repository.ProductRepository;
import com.example.sklepElektroniczny.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    CartRepository cartRepo;

    @Mock
    ProductRepository productRepo;

    @Mock
    CartElementRepository cartElementRepo;

    @Mock
    AuthUtil authUtil;

    @Mock
    ModelMapper mapper;

    @InjectMocks
    @Spy
    CartService cartService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addItemToCart_success() {
        Long productId = 1L;
        Integer count = 2;
        String userEmail = "test@example.com";

        Product product = new Product();
        product.setProductName("Laptop");
        product.setQuantity(5);
        product.setSpecialPrice(1000.0);
        product.setDiscount(10.0);

        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setTotalPrice(0.0);

        when(authUtil.getCurrentUserEmail()).thenReturn(userEmail);
        when(cartRepo.findCartByEmail(userEmail)).thenReturn(cart);
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(cartElementRepo.findCartElementByProductIdAndCartId(cart.getCartId(), productId)).thenReturn(null);
        when(mapper.map(any(Cart.class), eq(CartDTO.class))).thenAnswer(invocation -> {
            Cart source = invocation.getArgument(0);
            CartDTO dto = new CartDTO();
            dto.setTotalPrice(source.getTotalPrice());
            return dto;
        });

        CartDTO result = cartService.addItemToCart(productId, count);

        assertEquals(2000.0, result.getTotalPrice());
        verify(cartElementRepo).save(any(CartElement.class));
        verify(cartRepo).save(cart);
    }

    @Test
    void addItemToCart_productNotFound_throws() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.addItemToCart(1L, 1));
    }

    @Test
    void addItemToCart_productAlreadyInCart_throws() {
        Product product = new Product();
        product.setProductName("Phone");
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartElementRepo.findCartElementByProductIdAndCartId(anyLong(), eq(1L))).thenReturn(new CartElement());

        Cart cart = new Cart();
        cart.setCartId(1L);
        when(cartRepo.findCartByEmail(anyString())).thenReturn(cart);
        when(authUtil.getCurrentUserEmail()).thenReturn("a@b.com");

        assertThrows(APIException.class, () -> cartService.addItemToCart(1L, 1));
    }

    @Test
    void updateProductQuantityInCart_increaseQuantity_success() {
        String email = "a@b.com";
        Long cartId = 10L;
        Long productId = 1L;

        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setTotalPrice(100.0);

        Product product = new Product();
        product.setProductName("TV");
        product.setQuantity(10);
        product.setSpecialPrice(50.0);
        product.setDiscount(0.0);

        CartElement cartElement = new CartElement();
        cartElement.setQuantity(1);
        cartElement.setPrice(50.0);
        cartElement.setCartElementId(100L);

        when(authUtil.getCurrentUserEmail()).thenReturn(email);
        when(cartRepo.findCartByEmail(email)).thenReturn(cart);
        when(cartRepo.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(cartElementRepo.findCartElementByProductIdAndCartId(cartId, productId)).thenReturn(cartElement);
        when(cartElementRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        CartDTO updatedCart = cartService.updateProductQuantityInCart(productId, 2);

        assertNotNull(updatedCart);
        verify(cartElementRepo).save(cartElement);
        verify(cartRepo).save(cart);
        assertEquals(3, cartElement.getQuantity());
    }

    @Test
    void deleteProductFromCart_success() {
        Long cartId = 5L;
        Long productId = 10L;
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setTotalPrice(200.0);

        Product product = new Product();
        product.setProductName("Mouse");

        CartElement cartElement = new CartElement();
        cartElement.setPrice(50.0);
        cartElement.setQuantity(2);
        cartElement.setProduct(product);

        when(cartRepo.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartElementRepo.findCartElementByProductIdAndCartId(cartId, productId)).thenReturn(cartElement);

        String result = cartService.deleteProductFromCart(cartId, productId);

        assertTrue(result.contains("Mouse"));
        verify(cartElementRepo).deleteCartItemByProductIdAndCartId(cartId, productId);
    }

    @Test
    void deleteProductFromCart_productNotFound_throws() {
        when(cartRepo.findById(anyLong())).thenReturn(Optional.of(new Cart()));
        when(cartElementRepo.findCartElementByProductIdAndCartId(anyLong(), anyLong())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.deleteProductFromCart(1L, 1L));
    }
}

