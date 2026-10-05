package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySummaryResponse {

    private Long clientId;
    private String fullName;
    private String email;

    // الطلبات
    private long totalOrders;
    private double totalSpent;

    // الاستعارة
    private long totalBorrows;
    private long activeBorrows;
    private double totalFinesAccumulated;

    // التقييمات
    private long totalReviews;

    // الحجوزات
    private long totalReservations;
    private long activeReservations;
}
