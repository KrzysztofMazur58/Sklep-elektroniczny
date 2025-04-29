package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.ProductDTO;
import com.example.sklepElektroniczny.dtos.ProductResponse;
import com.example.sklepElektroniczny.exceptions.MyGlobalExceptionHandler;
import com.example.sklepElektroniczny.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new MyGlobalExceptionHandler())
                .build();
    }

    // -------------------- POST --------------------

    @Test
    public void testAddProduct_Success() throws Exception {
        ProductDTO request = new ProductDTO();
        request.setProductName("Laptop");

        ProductDTO response = new ProductDTO();
        response.setProductId(1L);
        response.setProductName("Laptop");

        when(productService.addProduct(anyLong(), any(ProductDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/categories/1/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Laptop\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    // -------------------- GET --------------------

    @Test
    public void testGetAllProducts_Success() throws Exception {
        ProductResponse response = new ProductResponse();
        response.setPageNumber(0);
        response.setPageSize(10);

        when(productService.getAllProducts(0, 10, "name", "asc", null, null)).thenReturn(response);

        mockMvc.perform(get("/api/public/products")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        ProductDTO product = new ProductDTO();
        product.setProductId(1L);
        product.setProductName("Laptop");

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/public/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/public/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductsByKeyword_Success() throws Exception {
        ProductResponse response = new ProductResponse();
        response.setPageNumber(0);

        when(productService.searchProductByKeyword("laptop", 0, 10, "name", "asc")).thenReturn(response);

        mockMvc.perform(get("/api/public/products/keyword/laptop")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    // -------------------- PUT --------------------

    @Test
    public void testUpdateProduct_Success() throws Exception {
        ProductDTO response = new ProductDTO();
        response.setProductId(2L);
        response.setProductName("Tablet");

        when(productService.updateProduct(anyLong(), any(ProductDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/products/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Tablet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(2))
                .andExpect(jsonPath("$.productName").value("Tablet"));
    }

    @Test
    public void testUpdateProductImage_Success() throws Exception {
        ProductDTO updated = new ProductDTO();
        updated.setProductId(1L);
        updated.setProductName("Nowy Laptop");

        MockMultipartFile file = new MockMultipartFile(
                "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());

        when(productService.updateProductImage(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(multipart("/api/products/1/image")
                        .file(file)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }

    // -------------------- DELETE --------------------

    @Test
    public void testDeleteProduct_Success() throws Exception {
        ProductDTO deleted = new ProductDTO();
        deleted.setProductId(3L);
        deleted.setProductName("Usunięty produkt");

        when(productService.deleteProduct(3L)).thenReturn(deleted);

        mockMvc.perform(delete("/api/admin/products/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(3))
                .andExpect(jsonPath("$.productName").value("Usunięty produkt"));
    }
}
