package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.CartDTO;
import com.example.sklepElektroniczny.dtos.ProductDTO;
import com.example.sklepElektroniczny.entity.Cart;
import com.example.sklepElektroniczny.entity.CartElement;
import com.example.sklepElektroniczny.entity.Product;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.CartElementRepository;
import com.example.sklepElektroniczny.repository.CartRepository;
import com.example.sklepElektroniczny.repository.ProductRepository;
import com.example.sklepElektroniczny.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartService implements CartServiceInterface {
    private final CartRepository cartRepo;
    private final AuthUtil authUtil;
    private final ProductRepository productRepo;
    private final CartElementRepository cartElementRepo;
    private final ModelMapper mapper;

    public CartService(CartRepository cartRepo, AuthUtil authUtil, ProductRepository productRepo,
                       CartElementRepository cartElementRepo, ModelMapper mapper) {
        this.cartRepo = cartRepo;
        this.authUtil = authUtil;
        this.productRepo = productRepo;
        this.cartElementRepo = cartElementRepo;
        this.mapper = mapper;
    }

    @Override
    public CartDTO addItemToCart(Long elementId, Integer count) {
        Cart cart = createCart();

        Product product = productRepo.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", elementId));

        CartElement existingCartElement = cartElementRepo.findCartElementByProductIdAndCartId(cart.getCartId(), elementId);

        if (existingCartElement != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < count) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartElement newCartElement = new CartElement();
        newCartElement.setProduct(product);
        newCartElement.setCart(cart);
        newCartElement.setQuantity(count);
        newCartElement.setDiscount(product.getDiscount());
        newCartElement.setPrice(product.getSpecialPrice());

        cartElementRepo.save(newCartElement);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * count));

        cartRepo.save(cart);
        CartDTO cartResponse = mapper.map(cart, CartDTO.class);

        List<CartElement> cartElements = cart.getCartElements();
        Stream<ProductDTO> productStream = cartElements.stream().map(element -> {
            ProductDTO productDto = mapper.map(element.getProduct(), ProductDTO.class);
            productDto.setQuantity(element.getQuantity());
            return productDto;
        });

        cartResponse.setProducts(productStream.toList());
        return cartResponse;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepo.findAll();

        if (carts.size() == 0) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = mapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartElements().stream().map(cartItem -> {
                ProductDTO productDTO = mapper.map(cartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity());
                return productDTO;
            }).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepo.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO = mapper.map(cart, CartDTO.class);
        cart.getCartElements().forEach(c ->
                c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartElements().stream()
                .map(p -> mapper.map(p.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, int quantity) {
        String emailId = authUtil.getCurrentUserEmail();
        Cart userCart = cartRepo.findCartByEmail(emailId);
        Long cartId  = userCart.getCartId();

        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartElement cartElement = cartElementRepo.findCartElementByProductIdAndCartId(cartId, productId);

        if (cartElement == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart");
        }

        int newQuantity = cartElement.getQuantity() + quantity;

        if(newQuantity < 0){
            throw new APIException("The quantity can't be less than 0");
        }

        if(newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        } else {

            cartElement.setPrice(product.getSpecialPrice());
            cartElement.setQuantity(cartElement.getQuantity() + quantity);
            cartElement.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartElement.getPrice() * quantity));
            cartRepo.save(cart);
        }
        CartElement updatedElement = cartElementRepo.save(cartElement);
        if(updatedElement.getQuantity() == 0){
            cartElementRepo.deleteById(updatedElement.getCartElementId());
        }


        CartDTO cartDTO = mapper.map(cart, CartDTO.class);

        List<CartElement> cartItems = cart.getCartElements();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = mapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });


        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartElement cartItem = cartElementRepo.findCartElementByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getPrice() * cartItem.getQuantity()));

        cartElementRepo.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    private Cart createCart() {
        Cart userCart = cartRepo.findCartByEmail(authUtil.getCurrentUserEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.getCurrentUser());
        return cartRepo.save(cart);
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartElement cartItem = cartElementRepo.findCartElementByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getPrice() * cartItem.getQuantity());

        cartItem.setPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getPrice() * cartItem.getQuantity()));

        cartItem = cartElementRepo.save(cartItem);
    }
}

