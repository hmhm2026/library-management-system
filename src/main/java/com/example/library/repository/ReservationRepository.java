package com.example.library.repository;

import com.example.library.entity.Reservation;
import com.example.library.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByClientId(Long clientId);

    long countByClientId(Long clientId);

    long countByClientIdAndStatus(Long clientId, ReservationStatus status);

    // الأقدم أولًا - عشان نفضّل أول حد حجز لما الكتاب يتوفر (First-Come-First-Served)
    List<Reservation> findByBookIdAndStatusOrderByReservationDateAsc(Long bookId, ReservationStatus status);

    boolean existsByClientIdAndBookIdAndStatus(Long clientId, Long bookId, ReservationStatus status);

    // بيستخدم لمعرفة كل الكتب اللي عليها حجوزات واقفة، عشان نزامن حالتها
    List<Reservation> findByStatus(ReservationStatus status);
}
