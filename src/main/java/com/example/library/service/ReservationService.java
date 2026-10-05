package com.example.library.service;

import com.example.library.entity.*;
import com.example.library.exception.BusinessRuleException;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.InventoryRepository;
import com.example.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * حجز كتاب - مسموح بس لو الكتاب فعلاً خلص من المخزون (availableCopies == 0).
     * لو لسه متاح، مفيش داعي للحجز خالص - العميل يستعير/يشتري عادي.
     */
    public Reservation createReservation(User client, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + bookId + " was not found"));

        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory record found for book id " + bookId));

        if (inventory.getAvailableCopies() > 0) {
            throw new BusinessRuleException(
                    "'" + book.getTitle() + "' is currently available (" + inventory.getAvailableCopies() +
                            " copies). No need to reserve it - just borrow or purchase it directly.");
        }

        if (reservationRepository.existsByClientIdAndBookIdAndStatus(
                client.getId(), bookId, ReservationStatus.WAITING)) {
            throw new DuplicateResourceException(
                    "You already have an active reservation for '" + book.getTitle() + "'.");
        }

        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setBook(book);
        return reservationRepository.save(reservation);
    }

    public void cancelReservation(Long id, User client) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with id " + id + " was not found"));

        if (!reservation.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("This reservation does not belong to you");
        }
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new BusinessRuleException("Only a waiting reservation can be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public List<Reservation> getMyReservations(Long clientId) {
        syncFulfillments();
        return reservationRepository.findByClientId(clientId);
    }

    public List<Reservation> getAllReservations() {
        syncFulfillments();
        return reservationRepository.findAll();
    }

    /**
     * بتتنفذ Lazy قبل أي عرض: بتدوّر على كل الكتب اللي عليها حجوزات WAITING،
     * ولو المخزون بقى متاح، بتحوّل أقدم حجوزات بعدد النسخ المتاحة لـ FULFILLED
     * (إشعار إن الكتاب بقى متاح - أول واحد حجز بيتقدّم الأول).
     */
    private void syncFulfillments() {
        List<Reservation> waiting = reservationRepository.findByStatus(ReservationStatus.WAITING);
        List<Long> bookIds = waiting.stream().map(r -> r.getBook().getId()).distinct().collect(Collectors.toList());

        for (Long bookId : bookIds) {
            Inventory inventory = inventoryRepository.findByBookId(bookId).orElse(null);
            if (inventory == null || inventory.getAvailableCopies() <= 0) {
                continue;
            }

            List<Reservation> queue = reservationRepository
                    .findByBookIdAndStatusOrderByReservationDateAsc(bookId, ReservationStatus.WAITING);

            int toFulfill = Math.min(inventory.getAvailableCopies(), queue.size());
            for (int i = 0; i < toFulfill; i++) {
                Reservation r = queue.get(i);
                r.setStatus(ReservationStatus.FULFILLED);
                r.setFulfilledDate(LocalDateTime.now());
                reservationRepository.save(r);
            }
        }
    }
}
