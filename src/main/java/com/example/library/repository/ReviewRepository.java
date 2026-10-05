package com.example.library.repository;

import com.example.library.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    List<Review> findByClientId(Long clientId);

    long countByClientId(Long clientId);

    boolean existsByClientIdAndBookId(Long clientId, Long bookId);

    Optional<Review> findByClientIdAndBookId(Long clientId, Long bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(Long bookId);
}
