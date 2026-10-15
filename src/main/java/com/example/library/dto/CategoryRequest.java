package com.example.library.dto;

import com.example.library.validation.LettersOnly;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    @LettersOnly
    private String name;

    @Size(max = 500)
    @LettersOnly
    private String description;
}

