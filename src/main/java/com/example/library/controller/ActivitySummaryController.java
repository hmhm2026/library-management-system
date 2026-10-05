package com.example.library.controller;

import com.example.library.dto.ActivitySummaryResponse;
import com.example.library.entity.User;
import com.example.library.service.ActivitySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base URL: /api/users
 * Endpoint إحصائي مجمّع - بيلمّ بيانات من Order, BorrowRecord, Review,
 * Reservation من غير ما يحتاج جدول جديد خالص.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Activity Summary", description = "Aggregated statistics per user across orders, borrows, reviews, and reservations")
public class ActivitySummaryController {

    private final ActivitySummaryService activitySummaryService;

    @Operation(summary = "Get my activity summary",
            description = "Aggregated stats for the currently authenticated user: total orders and spending, " +
                    "borrow history and active borrows, accumulated fines, reviews written, and reservations.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/activity-summary")
    public ResponseEntity<ActivitySummaryResponse> getMyActivitySummary(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(activitySummaryService.getSummary(currentUser.getId()));
    }

    @Operation(summary = "Get a specific user's activity summary (admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/activity-summary")
    public ResponseEntity<ActivitySummaryResponse> getUserActivitySummary(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(activitySummaryService.getSummary(id));
    }
}
