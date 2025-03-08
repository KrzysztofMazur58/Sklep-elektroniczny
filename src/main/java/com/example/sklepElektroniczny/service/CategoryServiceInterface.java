package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.entity.Category;

import java.util.List;

public interface CategoryServiceInterface {

    List<Category> getAllCategories();
    void createCategory(Category category);
    String deleteCategory(Long categoryId);
    Category updateCategory(Category category, Long categoryId);
}
