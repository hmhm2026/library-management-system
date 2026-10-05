package com.example.library.controller;

import com.example.library.dto.InventoryRequest;
import com.example.library.dto.UpdateInventoryRequest;
import com.example.library.entity.Inventory;
import com.example.library.service.InventoryService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Base URL: /api/inventory
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Book stock levels — one record per book (1:1)")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get all inventory records", description = "Public — no authentication required")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @Operation(summary = "Get a single inventory record by ID", description = "Public")
    @ApiResponse(responseCode = "200", description = "Record found")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(
            @Parameter(description = "Inventory record ID") @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @Operation(summary = "Get inventory by book ID", description = "Public")
    @ApiResponse(responseCode = "200", description = "Record found")
    @ApiResponse(responseCode = "404", description = "This book has no inventory record yet")
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Inventory> getInventoryByBookId(
            @Parameter(description = "Book ID") @PathVariable Long bookId) {
        return ResponseEntity.ok(inventoryService.getInventoryByBookId(bookId));
    }

    @Operation(summary = "Create an inventory record for a book",
            description = "ADMIN only. Each book can have only ONE inventory record (one-to-one).")
    @ApiResponse(responseCode = "201", description = "Created successfully")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @ApiResponse(responseCode = "409", description = "This book already has an inventory record")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@Valid @RequestBody InventoryRequest request) {
        Inventory saved = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Update available copies for a book", description = "ADMIN only.")
    @ApiResponse(responseCode = "200", description = "Updated successfully")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(
            @Parameter(description = "Inventory record ID") @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, request.getAvailableCopies()));
    }

    @Operation(summary = "Delete an inventory record", description = "ADMIN only.")
    @ApiResponse(responseCode = "204", description = "Deleted successfully")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "Inventory record ID") @PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}
