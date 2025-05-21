package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.CategoryDTO;
import com.example.sklepElektroniczny.dtos.CategoryResponse;
import com.example.sklepElektroniczny.entity.Category;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCategories_ReturnsCategoryResponse() {
        int pageNumber = 0;
        int pageSize = 2;
        String sortBy = "categoryName";
        String sortOrder = "asc";

        Category category1 = new Category();
        category1.setCategoryId(1L);
        category1.setCategoryName("Electronics");

        Category category2 = new Category();
        category2.setCategoryId(2L);
        category2.setCategoryName("Books");

        List<Category> categories = Arrays.asList(category1, category2);
        Page<Category> page = new PageImpl<>(categories,
                PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending()), 2);

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(category1, CategoryDTO.class)).thenReturn(new CategoryDTO(1L, "Electronics"));
        when(modelMapper.map(category2, CategoryDTO.class)).thenReturn(new CategoryDTO(2L, "Books"));

        CategoryResponse response = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPageNumber()).isEqualTo(pageNumber);
        assertThat(response.getPageSize()).isEqualTo(pageSize);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.isLastPage()).isTrue();
    }

    @Test
    void getAllCategories_ThrowsAPIException_WhenNoCategories() {
        Page<Category> emptyPage = new PageImpl<>(Collections.emptyList());
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        assertThatThrownBy(() -> categoryService.getAllCategories(0, 5, "categoryName", "asc"))
                .isInstanceOf(APIException.class)
                .hasMessage("No category created");
    }

    @Test
    void createCategory_Success() {
        CategoryDTO inputDto = new CategoryDTO(null, "New Category");
        Category categoryEntity = new Category();
        categoryEntity.setCategoryName("New Category");

        Category savedCategory = new Category();
        savedCategory.setCategoryId(1L);
        savedCategory.setCategoryName("New Category");

        CategoryDTO savedDto = new CategoryDTO(1L, "New Category");

        when(modelMapper.map(inputDto, Category.class)).thenReturn(categoryEntity);
        when(categoryRepository.findByCategoryName("New Category")).thenReturn(null);
        when(categoryRepository.save(categoryEntity)).thenReturn(savedCategory);
        when(modelMapper.map(savedCategory, CategoryDTO.class)).thenReturn(savedDto);

        CategoryDTO result = categoryService.createCategory(inputDto);

        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("New Category");
    }

    @Test
    void createCategory_ThrowsAPIException_WhenCategoryExists() {
        CategoryDTO inputDto = new CategoryDTO(null, "Existing Category");
        Category existingCategory = new Category();
        existingCategory.setCategoryName("Existing Category");

        when(modelMapper.map(inputDto, Category.class)).thenReturn(existingCategory);
        when(categoryRepository.findByCategoryName("Existing Category")).thenReturn(existingCategory);

        assertThatThrownBy(() -> categoryService.createCategory(inputDto))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void deleteCategory_Success() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName("Category to delete");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(new CategoryDTO(categoryId, "Category to delete"));

        CategoryDTO deletedDto = categoryService.deleteCategory(categoryId);

        assertThat(deletedDto.getCategoryId()).isEqualTo(categoryId);
        assertThat(deletedDto.getCategoryName()).isEqualTo("Category to delete");
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_ThrowsResourceNotFoundException_WhenCategoryNotFound() {
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void updateCategory_Success() {
        Long categoryId = 1L;
        CategoryDTO updateDto = new CategoryDTO(null, "Updated Name");
        Category existingCategory = new Category();
        existingCategory.setCategoryId(categoryId);
        existingCategory.setCategoryName("Old Name");

        Category categoryToSave = new Category();
        categoryToSave.setCategoryId(categoryId);
        categoryToSave.setCategoryName("Updated Name");

        Category savedCategory = new Category();
        savedCategory.setCategoryId(categoryId);
        savedCategory.setCategoryName("Updated Name");

        CategoryDTO savedDto = new CategoryDTO(categoryId, "Updated Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(modelMapper.map(updateDto, Category.class)).thenReturn(categoryToSave);
        when(categoryRepository.save(categoryToSave)).thenReturn(savedCategory);
        when(modelMapper.map(savedCategory, CategoryDTO.class)).thenReturn(savedDto);

        CategoryDTO result = categoryService.updateCategory(updateDto, categoryId);

        assertThat(result.getCategoryId()).isEqualTo(categoryId);
        assertThat(result.getCategoryName()).isEqualTo("Updated Name");
    }

    @Test
    void updateCategory_ThrowsResourceNotFoundException_WhenCategoryNotFound() {
        Long categoryId = 1L;
        CategoryDTO updateDto = new CategoryDTO(null, "Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(updateDto, categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

}

