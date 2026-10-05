package com.example.library.controller;

import com.example.library.dto.BorrowRequest;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.User;
import com.example.library.service.BorrowService;
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

/**
 * Base URL: /api/borrows
 * كل الـ Endpoints هنا محتاجة تسجيل دخول (مفيش تصفح عام).
 * الاستعارة والإرجاع ذاتي خدمة بالكامل - كل مستخدم بيتصرف في سجلاته هو بس.
 */
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Borrowing", description = "Self-service book borrowing and returns")
public class BorrowController {

    private final BorrowService borrowService;

    @Operation(summary = "Borrow a book",
            description = "Self-service — borrows the book for the currently authenticated user. " +
                    "Rejected if: no copies available, the user already has 3 active borrows, " +
                    "or the user already has this exact book borrowed.")
    @ApiResponse(responseCode = "201", description = "Borrowed successfully")
    @ApiResponse(responseCode = "400", description = "Business rule violated (no stock / limit reached / already borrowed)")
    @ApiResponse(responseCode = "404", description = "Book or inventory record not found")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<BorrowRecord> borrowBook(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody BorrowRequest request) {
        BorrowRecord record = borrowService.borrowBook(currentUser, request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @Operation(summary = "Return a borrowed book",
            description = "Self-service — only the borrower themself can return it. " +
                    "Automatically calculates a late fine (50 per day) if returned after the due date.")
    @ApiResponse(responseCode = "200", description = "Returned successfully")
    @ApiResponse(responseCode = "400", description = "This book was already returned")
    @ApiResponse(responseCode = "403", description = "This borrow record does not belong to you")
    @ApiResponse(responseCode = "404", description = "Borrow record not found")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowRecord> returnBook(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Borrow record ID") @PathVariable Long id) {
        return ResponseEntity.ok(borrowService.returnBook(id, currentUser));
    }

    @Operation(summary = "Get my borrow history", description = "Returns all borrow records for the current user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<BorrowRecord>> getMyBorrows(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(borrowService.getMyBorrows(currentUser.getId()));
    }

    @Operation(summary = "Get a single borrow record",
            description = "Only the owner of the record or an admin can view it.")
    @ApiResponse(responseCode = "200", description = "Record found")
    @ApiResponse(responseCode = "403", description = "This borrow record does not belong to you")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<BorrowRecord> getBorrowById(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Borrow record ID") @PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getBorrowById(id, currentUser));
    }

    @Operation(summary = "Get all borrow records (admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<BorrowRecord>> getAllBorrows() {
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }
}
