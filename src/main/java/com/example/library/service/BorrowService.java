package com.example.library.service;

import com.example.library.entity.*;
import com.example.library.exception.BusinessRuleException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowService {

    private static final List<BorrowStatus> ACTIVE_STATUSES = List.of(BorrowStatus.ACTIVE, BorrowStatus.OVERDUE);

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * استعارة كتاب - بيطبق كل القواعد بالترتيب المتفق عليه:
     * 1. فيه نسخ متاحة؟  2. العميل تحت حد الـ 3 كتب؟  3. العميل معندوش نفس الكتاب مستعار بالفعل؟
     */
    public BorrowRecord borrowBook(User client, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + bookId + " was not found"));

        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory record found for book id " + bookId));

        if (inventory.getAvailableCopies() < 1) {
            throw new BusinessRuleException(
                    "No copies of '" + book.getTitle() + "' are currently available to borrow.");
        }

        long activeCount = borrowRecordRepository.countByClientIdAndStatusIn(client.getId(), ACTIVE_STATUSES);
        if (activeCount >= BorrowRecord.MAX_ACTIVE_BORROWS_PER_CLIENT) {
            throw new BusinessRuleException(
                    "You already have " + BorrowRecord.MAX_ACTIVE_BORROWS_PER_CLIENT +
                            " active borrows. Return a book before borrowing another.");
        }

        boolean alreadyBorrowed = borrowRecordRepository.existsByClientIdAndBookIdAndStatusIn(
                client.getId(), bookId, ACTIVE_STATUSES);
        if (alreadyBorrowed) {
            throw new BusinessRuleException(
                    "You already have an active borrow for '" + book.getTitle() + "'. " +
                            "Return it before borrowing another copy of the same title.");
        }

        // كل الشروط اتحققت: خصم الكمية وإنشاء السجل
        inventory.setAvailableCopies(inventory.getAvailableCopies() - 1);
        inventoryRepository.save(inventory);

        BorrowRecord record = new BorrowRecord();
        record.setClient(client);
        record.setBook(book);
        return borrowRecordRepository.save(record);
    }

    /**
     * إرجاع كتاب - بيتحقق إن السجل فعلاً بتاع نفس العميل، يحسب الغرامة لو متأخر،
     * ويرجّع الكمية للمخزون.
     */
    public BorrowRecord returnBook(Long borrowRecordId, User client) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Borrow record with id " + borrowRecordId + " was not found"));

        if (!record.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("This borrow record does not belong to you");
        }

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BusinessRuleException("This book has already been returned.");
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(record.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
            record.setFineAmount(daysLate * BorrowRecord.FINE_PER_DAY);
        }

        record.setReturnDate(today);
        record.setStatus(BorrowStatus.RETURNED);

        // رجّع الكمية للمخزون
        Inventory inventory = inventoryRepository.findByBookId(record.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory record found for book id " + record.getBook().getId()));
        inventory.setAvailableCopies(inventory.getAvailableCopies() + 1);
        inventoryRepository.save(inventory);

        return borrowRecordRepository.save(record);
    }

    public List<BorrowRecord> getMyBorrows(Long clientId) {
        syncOverdueStatuses();
        return borrowRecordRepository.findByClientId(clientId);
    }

    public List<BorrowRecord> getAllBorrows() {
        syncOverdueStatuses();
        return borrowRecordRepository.findAll();
    }

    public BorrowRecord getBorrowById(Long id, User requester) {
        syncOverdueStatuses();
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record with id " + id + " was not found"));

        boolean isOwner = record.getClient().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("This borrow record does not belong to you");
        }
        return record;
    }

    /**
     * بتحدّث أي سجل ACTIVE فات معاده الحالي لـ OVERDUE - بتتنفذ Lazy قبل أي
     * عملية عرض، عشان الحالة تبقى دقيقة من غير الحاجة لـ Scheduled Job منفصل.
     */
    private void syncOverdueStatuses() {
        List<BorrowRecord> overdue = borrowRecordRepository.findByStatusAndDueDateBefore(
                BorrowStatus.ACTIVE, LocalDate.now());
        for (BorrowRecord record : overdue) {
            record.setStatus(BorrowStatus.OVERDUE);
        }
        if (!overdue.isEmpty()) {
            borrowRecordRepository.saveAll(overdue);
        }
    }
}
