package com.example.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * سجل استعارة واحد = عميل استعار كتاب واحد.
 * dueDate بيتحدد تلقائيًا = borrowDate + 14 يوم.
 * fineAmount بيتحسب وقت الإرجاع بس (لو كان متأخر): أيام التأخير × 50 جنيه.
 */
@Entity
@Table(name = "borrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    public static final int BORROW_PERIOD_DAYS = 14;
    public static final double FINE_PER_DAY = 50.0;
    public static final int MAX_ACTIVE_BORROWS_PER_CLIENT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false, updatable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;

    @Column(name = "fine_amount")
    private Double fineAmount;

    @PrePersist
    protected void onCreate() {
        borrowDate = LocalDate.now();
        dueDate = borrowDate.plusDays(BORROW_PERIOD_DAYS);
        status = BorrowStatus.ACTIVE;
        fineAmount = 0.0;
    }
}
