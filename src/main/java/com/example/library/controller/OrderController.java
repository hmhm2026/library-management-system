package com.example.library.controller;

import com.example.library.dto.OrderRequest;
import com.example.library.entity.Order;
import com.example.library.entity.User;
import com.example.library.service.OrderService;
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
 * Base URL: /api/orders
 * شراء ذاتي خدمة (Self-checkout) بالكامل - الكلاينت بيشتري لنفسه بس.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Self-checkout purchases (shopping cart of books)")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place an order (checkout)",
            description = "Self-service — creates the order for the currently authenticated user. " +
                    "Accepts multiple books and quantities in one request (a cart). " +
                    "Every item is validated against inventory BEFORE any stock is deducted — " +
                    "if any single item is unavailable in the requested quantity, the WHOLE order is rejected. " +
                    "totalPrice is calculated automatically and can't be sent by the client.")
    @ApiResponse(responseCode = "201", description = "Order placed successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient stock for one or more items")
    @ApiResponse(responseCode = "404", description = "A book or its inventory record was not found")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody OrderRequest request) {
        Order order = orderService.createOrder(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Operation(summary = "Get my order history")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(orderService.getMyOrders(currentUser.getId()));
    }

    @Operation(summary = "Get a single order", description = "Only the owner or an admin can view it.")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "403", description = "This order does not belong to you")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, currentUser));
    }

    @Operation(summary = "Get all orders (admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
