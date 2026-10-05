package com.example.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ملحوظة عن isbn: هنا مجرد رقم تسلسلي بسيط بيبدأ من 1 ويزيد تلقائيًا
 * (زي أي id)، مش رقم ISBN الدولي الحقيقي بـ 13 رقم. الهدف بس إنه معرّف
 * فريد وواضح لكل كتاب، مش تطبيق معيار الناشرين الرسمي.
 */
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private Long isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 150)
    @Column(nullable = false)
    private String author;

    @NotNull(message = "Publication year is required")
    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Size(max = 150)
    private String publisher;

    @Size(max = 100)
    private String genre;

    // اختياري - كتاب ممكن يفضل من غير تصنيف رسمي، وده منفصل عن genre النصي أعلاه
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive number")
    @Column(nullable = false)
    private Double price;

    @Size(max = 1000)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
