package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.configuration.AppConstants;
import com.example.sklepElektroniczny.dtos.CategoryDTO;
import com.example.sklepElektroniczny.dtos.CategoryResponse;
import com.example.sklepElektroniczny.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Pobierz wszystkie kategorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pomyślnie pobrano kategorie"),
            @ApiResponse(responseCode = "400", description = "Błąd w żądaniu")
    })
    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @Parameter(description = "Numer strony", example = "0")
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Parameter(description = "Rozmiar strony", example = "10")
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @Parameter(description = "Sortuj po", example = "name")
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @Parameter(description = "Kierunek sortowania", example = "asc")
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder){

        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @Operation(summary = "Utwórz nową kategorię")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Kategoria utworzona pomyślnie"),
            @ApiResponse(responseCode = "422", description = "Błąd walidacji danych wejściowych")
    })
    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {

            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Usuń kategorię")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kategoria usunięta pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie znaleziona")
    })
    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory( @Parameter(description = "ID kategorii", example = "1") @PathVariable Long categoryId){
        CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }

    @Operation(summary = "Aktualizuj kategorię")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kategoria zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "422", description = "Błąd walidacji danych wejściowych")
    })
    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @Parameter(description = "ID kategorii", example = "1") @PathVariable Long categoryId){
        CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
    }
}
