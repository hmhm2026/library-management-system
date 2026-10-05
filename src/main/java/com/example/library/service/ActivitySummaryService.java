package com.example.library.service;

import com.example.library.dto.ActivitySummaryResponse;
import com.example.library.entity.BorrowStatus;
import com.example.library.entity.OrderStatus;
import com.example.library.entity.ReservationStatus;
import com.example.library.entity.User;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivitySummaryService {

    private static final List<BorrowStatus> ACTIVE_BORROW_STATUSES = List.of(BorrowStatus.ACTIVE, BorrowStatus.OVERDUE);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    public ActivitySummaryResponse getSummary(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + clientId + " was not found"));

        ActivitySummaryResponse summary = new ActivitySummaryResponse();
        summary.setClientId(client.getId());
        summary.setFullName(client.getFullName());
        summary.setEmail(client.getEmail());

        summary.setTotalOrders(orderRepository.countByClientId(clientId));
        summary.setTotalSpent(orderRepository.sumTotalPriceByClientIdAndStatus(clientId, OrderStatus.COMPLETED));

        summary.setTotalBorrows(borrowRecordRepository.countByClientId(clientId));
        summary.setActiveBorrows(borrowRecordRepository.countByClientIdAndStatusIn(clientId, ACTIVE_BORROW_STATUSES));
        summary.setTotalFinesAccumulated(borrowRecordRepository.sumFineAmountByClientId(clientId));

        summary.setTotalReviews(reviewRepository.countByClientId(clientId));

        summary.setTotalReservations(reservationRepository.countByClientId(clientId));
        summary.setActiveReservations(
                reservationRepository.countByClientIdAndStatus(clientId, ReservationStatus.WAITING));

        return summary;
    }
}
