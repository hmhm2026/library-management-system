package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // بيستخدم لحساب رقم الـ isbn التالي (أعلى قيمة حالية + 1)
    @Query("SELECT COALESCE(MAX(b.isbn), 0) FROM Book b")
    Long findMaxIsbn();
    // بيستخدم قبل حذف أي تصنيف، عشان نمنع حذف تصنيف لسه ليه كتب مرتبطة بيه
    boolean existsByCategoryId(Long categoryId);
}
