package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.configuration.AppConstants;
import com.example.sklepElektroniczny.dtos.ProductDTO;
import com.example.sklepElektroniczny.dtos.ProductResponse;
import com.example.sklepElektroniczny.entity.Product;
import com.example.sklepElektroniczny.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @Operation(summary = "Dodaj nowy produkt do kategorii")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produkt został utworzony pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych wejściowych")
    })
    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @Parameter(description = "ID kategorii", example = "1")
                                                 @PathVariable Long categoryId){
        ProductDTO savedproductDTO = productService.addProduct(categoryId, productDTO);

        return new ResponseEntity<>(savedproductDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Pobierz wszystkie produkty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkty zostały pobrane pomyślnie")
    })
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder, keyword, category);

        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @Operation(summary = "Pobierz produkt po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został pobrany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Produkt nie znaleziony")
    })
    @GetMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@Parameter(description = "ID produktu", example = "1") @PathVariable Long productId) {
        ProductDTO productDTO = productService.getProductById(productId);
        if (productDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }


    @Operation(summary = "Pobierz produkty według kategorii")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkty zostały pobrane pomyślnie")
    })
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@Parameter(description = "ID kategorii", example = "1") @PathVariable Long categoryId,
                                                                 @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                 @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                 @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder){
        ProductResponse productResponse = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @Operation(summary = "Wyszukaj produkty według słowa kluczowego")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Produkty zostały znalezione"),
            @ApiResponse(responseCode = "404", description = "Brak produktów pasujących do kryteriów")
    })
    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@Parameter(description = "Słowo kluczowe", example = "laptop") @PathVariable String keyword,
                                                                @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                                @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                                @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
                                                                @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder){
        ProductResponse productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @Operation(summary = "Aktualizuj dane produktu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został zaktualizowany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Produkt nie znaleziony")
    })
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                    @Parameter(description = "ID produktu", example = "1") @PathVariable Long productId){
        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @Operation(summary = "Usuń produkt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt został usunięty pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Produkt nie znaleziony")
    })
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@Parameter(description = "ID produktu", example = "1") @PathVariable Long productId){
        ProductDTO deletedProduct = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }

    @Operation(summary = "Aktualizuj zdjęcie produktu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zdjęcie produktu zostało zaktualizowane"),
            @ApiResponse(responseCode = "404", description = "Produkt nie znaleziony")
    })
    @PutMapping("/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@Parameter(description = "ID produktu", example = "1") @PathVariable Long productId,
                                                         @RequestParam("image")MultipartFile image) throws IOException {
        ProductDTO updatedProduct = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }
}
