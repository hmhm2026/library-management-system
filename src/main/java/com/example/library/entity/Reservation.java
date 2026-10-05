package com.example.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * حجز واحد = عميل ينتظر كتاب خلص من المخزون. لما الكمية المتاحة ترجع
 * (عن طريق إرجاع استعارة أو تحديث الأدمن للمخزون)، أقدم حجز WAITING
 * بيتحول تلقائيًا لـ FULFILLED (إشعار إن الكتاب بقى متاح - مش حجز فعلي
 * للنسخة، لسه محتاج العميل يستعير/يشتري بالطريقة العادية).
 */
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "reservation_date", nullable = false, updatable = false)
    private LocalDateTime reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "fulfilled_date")
    private LocalDateTime fulfilledDate;

    @PrePersist
    protected void onCreate() {
        reservationDate = LocalDateTime.now();
        status = ReservationStatus.WAITING;
    }
}
