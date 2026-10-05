package com.example.library.controller;

import com.example.library.dto.CreateReviewRequest;
import com.example.library.dto.ReviewRequest;
import com.example.library.entity.Review;
import com.example.library.entity.User;
import com.example.library.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Base URL: /api/reviews
 * قراءة التقييمات عامة (زي تصفح الكتب). الإنشاء/التعديل/الحذف محتاجين تسجيل دخول.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Book reviews — public reading, verified purchase/borrow required to write")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get all reviews for a book", description = "Public — no authentication required")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsForBook(
            @Parameter(description = "Book ID") @PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewsForBook(bookId));
    }

    @Operation(summary = "Get the average rating for a book", description = "Public. Returns null if the book has no reviews yet.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @GetMapping("/book/{bookId}/average")
    public ResponseEntity<Map<String, Object>> getAverageRating(
            @Parameter(description = "Book ID") @PathVariable Long bookId) {
        Double average = reviewService.getAverageRatingForBook(bookId);
        return ResponseEntity.ok(Map.of("bookId", bookId, "averageRating", average == null ? "N/A" : average));
    }

    @Operation(summary = "Write a review",
            description = "Requires the current user to have purchased or borrowed this book at least once. " +
                    "Only one review per (client, book) pair is allowed — use PUT to edit an existing one.")
    @ApiResponse(responseCode = "201", description = "Review created successfully")
    @ApiResponse(responseCode = "400", description = "You haven't purchased or borrowed this book")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @ApiResponse(responseCode = "409", description = "You have already reviewed this book")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Review> createReview(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @Operation(summary = "Update my review", description = "Only the review's author can edit it.")
    @ApiResponse(responseCode = "200", description = "Updated successfully")
    @ApiResponse(responseCode = "403", description = "This review does not belong to you")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Review ID") @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, currentUser, request));
    }

    @Operation(summary = "Delete a review", description = "The author or an admin can delete it.")
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @ApiResponse(responseCode = "403", description = "This review does not belong to you")
    @ApiResponse(responseCode = "404", description = "Review not found")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Review ID") @PathVariable Long id) {
        reviewService.deleteReview(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get my reviews", description = "Returns all reviews written by the current user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<Review>> getMyReviews(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(reviewService.getMyReviews(currentUser.getId()));
    }
}
