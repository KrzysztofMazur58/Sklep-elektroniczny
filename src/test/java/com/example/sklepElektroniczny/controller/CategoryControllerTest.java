package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.CategoryDTO;
import com.example.sklepElektroniczny.dtos.CategoryResponse;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.MyGlobalExceptionHandler;
import com.example.sklepElektroniczny.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new MyGlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetAllCategories_Success() throws Exception {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setPageNumber(0);
        categoryResponse.setPageSize(10);
        categoryResponse.setTotalPages(1);
        categoryResponse.setLastPage(true);

        when(categoryService.getAllCategories(0, 10, "name", "asc")).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/public/categories")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    public void testGetAllCategories_NotFound() throws Exception {
        when(categoryService.getAllCategories(0, 10, "name", "asc"))
                .thenThrow(new APIException("No category created"));

        mockMvc.perform(get("/api/public/categories")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No category created"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    public void testCreateCategory_Success() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Laptopy");

        CategoryDTO response = new CategoryDTO();
        response.setCategoryId(1L);
        response.setCategoryName("Laptopy");

        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Laptopy\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Laptopy"));
    }

    @Test
    public void testCreateCategory_ValidationError() throws Exception {
        mockMvc.perform(post("/api/public/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testUpdateCategory_Success() throws Exception {
        CategoryDTO request = new CategoryDTO();
        request.setCategoryName("Tablety");

        CategoryDTO response = new CategoryDTO();
        response.setCategoryId(2L);
        response.setCategoryName("Tablety");

        when(categoryService.updateCategory(any(CategoryDTO.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(put("/api/public/categories/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"Tablety\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(2))
                .andExpect(jsonPath("$.categoryName").value("Tablety"));
    }

    @Test
    public void testDeleteCategory_Success() throws Exception {
        CategoryDTO deleted = new CategoryDTO();
        deleted.setCategoryId(3L);
        deleted.setCategoryName("Usunięta");

        when(categoryService.deleteCategory(3L)).thenReturn(deleted);

        mockMvc.perform(delete("/api/admin/categories/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(3))
                .andExpect(jsonPath("$.categoryName").value("Usunięta"));
    }
}
