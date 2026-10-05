package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * الشكل اللي العميل بيبعته فعليًا عند إضافة/تعديل كتاب.
 * مفيش isbn هنا خالص - بيتحدد تلقائيًا في الـ Service (أعلى رقم + 1).
 */
@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 150)
    private String author;

    @NotNull(message = "Publication year is required")
    private Integer publicationYear;

    @Size(max = 150)
    private String publisher;

    @Size(max = 100)
    private String genre;

    // اختياري - لو مبعتوش، الكتاب بيفضل من غير تصنيف
    private Long categoryId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive number")
    private Double price;

    @Size(max = 1000)
    private String description;
}
