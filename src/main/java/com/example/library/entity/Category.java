package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * كل كتاب ينتمي لفئة واحدة بس (اختياري - Book.category ممكن يفضل فاضي).
 * ده منفصل عن حقل Book.genre الموجود أصلاً: genre نص حر بسيط، أما Category
 * فمورد منظّم ليه CRUD خاص بيه وممكن الأدمن يدير الفئات المتاحة منه.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 500)
    private String description;
}
