package com.example.sklepElektroniczny.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryDTO {

    private Long categoryId;

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 2, message = "Category name must have at least 2 characters")
    private String categoryName;
}

