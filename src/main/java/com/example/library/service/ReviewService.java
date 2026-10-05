package com.example.library.service;

import com.example.library.dto.CreateReviewRequest;
import com.example.library.dto.ReviewRequest;
import com.example.library.entity.Book;
import com.example.library.entity.Review;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.exception.BusinessRuleException;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.OrderItemRepository;
import com.example.library.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * إنشاء تقييم - بيتحقق من قاعدتين:
     * 1. العميل فعلاً اشترى أو استعار الكتاب ده قبل كده (Verified Purchase).
     * 2. العميل مقيّمش نفس الكتاب قبل كده (مراجعة واحدة بس لكل عميل لكل كتاب).
     */
    public Review createReview(User client, CreateReviewRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book with id " + request.getBookId() + " was not found"));

        boolean hasBorrowed = borrowRecordRepository.existsByClientIdAndBookId(client.getId(), book.getId());
        boolean hasPurchased = orderItemRepository.existsByOrder_ClientIdAndBookId(client.getId(), book.getId());

        if (!hasBorrowed && !hasPurchased) {
            throw new BusinessRuleException(
                    "You can only review books you have purchased or borrowed at least once.");
        }

        if (reviewRepository.existsByClientIdAndBookId(client.getId(), book.getId())) {
            throw new DuplicateResourceException(
                    "You have already reviewed this book. Use PUT /api/reviews/{id} to update your review instead.");
        }

        Review review = new Review();
        review.setClient(client);
        review.setBook(book);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewRepository.save(review);
    }

    public Review updateReview(Long id, User client, ReviewRequest request) {
        Review existing = getReviewByIdInternal(id);
        if (!existing.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("This review does not belong to you");
        }
        existing.setRating(request.getRating());
        existing.setComment(request.getComment());
        return reviewRepository.save(existing);
    }

    public void deleteReview(Long id, User requester) {
        Review existing = getReviewByIdInternal(id);
        boolean isOwner = existing.getClient().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("This review does not belong to you");
        }
        reviewRepository.delete(existing);
    }

    public List<Review> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    public Double getAverageRatingForBook(Long bookId) {
        Double avg = reviewRepository.findAverageRatingByBookId(bookId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null; // تقريب لخانة عشرية واحدة
    }

    public List<Review> getMyReviews(Long clientId) {
        return reviewRepository.findByClientId(clientId);
    }

    private Review getReviewByIdInternal(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " was not found"));
    }
}
