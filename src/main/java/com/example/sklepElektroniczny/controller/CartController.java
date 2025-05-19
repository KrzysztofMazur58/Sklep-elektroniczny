package com.example.sklepElektroniczny.controller;


import com.example.sklepElektroniczny.dtos.CartDTO;
import com.example.sklepElektroniczny.entity.Cart;
import com.example.sklepElektroniczny.repository.CartRepository;
import com.example.sklepElektroniczny.service.CartService;
import com.example.sklepElektroniczny.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @Operation(summary = "Dodaj produkt do koszyka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produkt został dodany do koszyka"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addItemToCart(@Parameter(description = "ID produktu", example = "1") @PathVariable Long productId,
                                                 @Parameter(description = "Ilość produktu", example = "2") @PathVariable Integer quantity) {
        CartDTO cartResponse = cartService.addItemToCart(productId, quantity);
        return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Pobierz wszystkie koszyki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Koszyki zostały pobrane pomyślnie")
    })
    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOs, HttpStatus.FOUND);
    }

    @Operation(summary = "Pobierz koszyk aktualnie zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Koszyk użytkownika został pobrany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Koszyk nie znaleziony")
    })
    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartById(){
        String emailId = authUtil.getCurrentUserEmail();
        System.out.println("Fetching cart for user: " + emailId);
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();
        CartDTO cartDTO = cartService.getCart(emailId, cartId);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @Operation(summary = "Aktualizuj ilość produktu w koszyku")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ilość produktu została zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowa operacja")
    })
    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@Parameter(description = "ID produktu", example = "1") @PathVariable Long productId,
                                                     @Parameter(description = "Operacja: 'increase' lub 'delete'", example = "increase") @PathVariable String operation) {

        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @Operation(summary = "Usuń produkt z koszyka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został usunięty z koszyka"),
            @ApiResponse(responseCode = "404", description = "Produkt lub koszyk nie znaleziony")
    })
    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@Parameter(description = "ID koszyka", example = "1") @PathVariable Long cartId,
                                                        @Parameter(description = "ID produktu", example = "10") @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);

        return new ResponseEntity<String>(status, HttpStatus.OK);
    }
}
