package com.example.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateInventoryRequest {

    @NotNull(message = "Available copies is required")
    @PositiveOrZero(message = "Available copies cannot be negative")
    private Integer availableCopies;
}
