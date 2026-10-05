package com.example.library.controller;

import com.example.library.dto.ReservationRequest;
import com.example.library.entity.Reservation;
import com.example.library.entity.User;
import com.example.library.service.ReservationService;
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
 * Base URL: /api/reservations
 * كل الـ Endpoints هنا محتاجة تسجيل دخول - ذاتي خدمة بالكامل زي الاستعارة.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reservations", description = "Waitlist for books that are currently out of stock")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Reserve a book",
            description = "Only allowed when the book currently has ZERO available copies. " +
                    "If it has copies available, borrow or buy it directly instead.")
    @ApiResponse(responseCode = "201", description = "Reservation created successfully")
    @ApiResponse(responseCode = "400", description = "The book is currently available — no need to reserve")
    @ApiResponse(responseCode = "404", description = "Book or inventory record not found")
    @ApiResponse(responseCode = "409", description = "You already have an active reservation for this book")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(currentUser, request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @Operation(summary = "Cancel my reservation", description = "Only a WAITING reservation can be cancelled.")
    @ApiResponse(responseCode = "204", description = "Cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Reservation is not in WAITING status")
    @ApiResponse(responseCode = "403", description = "This reservation does not belong to you")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Reservation ID") @PathVariable Long id) {
        reservationService.cancelReservation(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get my reservations",
            description = "Status is synced automatically: a WAITING reservation turns into FULFILLED " +
                    "once the book becomes available again (oldest reservation first).")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(reservationService.getMyReservations(currentUser.getId()));
    }

    @Operation(summary = "Get all reservations (admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
}
