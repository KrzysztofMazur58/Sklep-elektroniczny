package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.CartDTO;
import com.example.sklepElektroniczny.dtos.ProductDTO;
import com.example.sklepElektroniczny.entity.Category;
import com.example.sklepElektroniczny.entity.Cart;
import com.example.sklepElektroniczny.entity.Product;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.rabbitmq.MessageProducer;
import com.example.sklepElektroniczny.repository.CartRepository;
import com.example.sklepElektroniczny.repository.CategoryRepository;
import com.example.sklepElektroniczny.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService.setImageBaseUrl("http://localhost/images");
    }

    @Test
    void addProduct_Success() {
        Long categoryId = 1L;
        ProductDTO inputDto = new ProductDTO();
        inputDto.setProductName("Test Product");
        inputDto.setPrice(100.0);
        inputDto.setDiscount(10);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setProducts(new ArrayList<>());

        Product productEntity = new Product();
        productEntity.setProductName("Test Product");
        productEntity.setPrice(100.0);
        productEntity.setDiscount(10);

        Product savedProduct = new Product();
        savedProduct.setProductId(1L);
        savedProduct.setProductName("Test Product");
        savedProduct.setPrice(100.0);
        savedProduct.setDiscount(10);
        savedProduct.setImage("default.png");
        savedProduct.setSpecialPrice(90.0);
        savedProduct.setCategory(category);

        ProductDTO savedDto = new ProductDTO();
        savedDto.setProductId(1L);
        savedDto.setProductName("Test Product");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(modelMapper.map(inputDto, Product.class)).thenReturn(productEntity);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductDTO.class)).thenReturn(savedDto);

        ProductDTO result = productService.addProduct(categoryId, inputDto);

        assertThat(result.getProductId()).isEqualTo(1L);
        verify(messageProducer).sendProductCreatedMessage("Dodano produkt: " + savedProduct.getProductName());
    }

    @Test
    void addProduct_ThrowsException_WhenProductExists() {
        Long categoryId = 1L;
        ProductDTO inputDto = new ProductDTO();
        inputDto.setProductName("Existing Product");

        Product existingProduct = new Product();
        existingProduct.setProductName("Existing Product");

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setProducts(List.of(existingProduct));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(modelMapper.map(inputDto, Product.class)).thenReturn(existingProduct);

        assertThatThrownBy(() -> productService.addProduct(categoryId, inputDto))
                .isInstanceOf(APIException.class)
                .hasMessage("Product already exists");
    }

    @Test
    void getProductById_Success() {
        Long productId = 1L;
        Product product = new Product();
        product.setProductId(productId);
        product.setProductName("Product");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setProductName("Product");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

        ProductDTO result = productService.getProductById(productId);

        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getProductName()).isEqualTo("Product");
    }

    @Test
    void getProductById_ThrowsResourceNotFoundException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProduct_ThrowsAPIException_WhenProductInCart() {
        Long productId = 1L;
        Product product = new Product();
        product.setProductId(productId);

        Cart cart = new Cart();
        cart.setCartId(10L);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findCartsByProductId(productId)).thenReturn(List.of(cart));

        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Nie można usunąć produktu");
    }

    @Test
    void deleteProduct_Success() {
        Long productId = 1L;
        Product product = new Product();
        product.setProductId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findCartsByProductId(productId)).thenReturn(Collections.emptyList());

        doNothing().when(productRepository).delete(product);
        when(modelMapper.map(product, ProductDTO.class)).thenReturn(new ProductDTO());

        ProductDTO result = productService.deleteProduct(productId);

        verify(productRepository).delete(product);
    }

    @Test
    void updateProduct_Success() {
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setProductId(productId);
        existingProduct.setProductName("Old Name");
        existingProduct.setPrice(100.0);

        ProductDTO updateDto = new ProductDTO();
        updateDto.setProductName("New Name");
        updateDto.setPrice(200.0);
        updateDto.setDiscount(5);
        updateDto.setSpecialPrice(190.0);

        Product mappedProduct = new Product();
        mappedProduct.setProductName("New Name");
        mappedProduct.setPrice(200.0);
        mappedProduct.setDiscount(5);
        mappedProduct.setSpecialPrice(190.0);

        Product savedProduct = new Product();
        savedProduct.setProductId(productId);
        savedProduct.setProductName("New Name");
        savedProduct.setPrice(200.0);
        savedProduct.setDiscount(5);
        savedProduct.setSpecialPrice(190.0);

        Cart cart = new Cart();
        cart.setCartId(10L);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(10L);

        ProductDTO savedDto = new ProductDTO();
        savedDto.setProductId(productId);
        savedDto.setProductName("New Name");

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(modelMapper.map(updateDto, Product.class)).thenReturn(mappedProduct);
        when(productRepository.save(existingProduct)).thenReturn(savedProduct);
        when(cartRepository.findCartsByProductId(productId)).thenReturn(List.of(cart));
        when(modelMapper.map(cart, CartDTO.class)).thenReturn(cartDTO);
        when(modelMapper.map(savedProduct, ProductDTO.class)).thenReturn(savedDto);

        ProductDTO result = productService.updateProduct(productId, updateDto);

        verify(cartService).updateProductInCarts(anyLong(), eq(productId));
        assertThat(result.getProductName()).isEqualTo("New Name");
    }

    @Test
    void updateProductImage_Success() throws IOException {
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setProductId(productId);
        existingProduct.setImage("old.png");

        Product savedProduct = new Product();
        savedProduct.setProductId(productId);
        savedProduct.setImage("new-image.png");

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(productId);
        productDTO.setImage("new-image.png");

        MockMultipartFile mockFile = new MockMultipartFile(
                "imageFile",
                "test.png",
                "image/png",
                "some-image-content".getBytes());

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductDTO.class)).thenReturn(productDTO);

        ProductDTO result = productService.updateProductImage(productId, mockFile);

        assertThat(result.getImage()).isEqualTo("new-image.png");
    }
}

