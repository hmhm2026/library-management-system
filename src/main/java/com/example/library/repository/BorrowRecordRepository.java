package com.example.library.repository;

import com.example.library.entity.BorrowRecord;
import com.example.library.entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByClientId(Long clientId);

    // بيستخدم للتحقق من حد الـ 3 كتب المستعارة في نفس الوقت
    long countByClientIdAndStatusIn(Long clientId, List<BorrowStatus> statuses);

    // بيستخدم للتحقق من عدم استعارة نفس الكتاب مرتين في نفس الوقت
    boolean existsByClientIdAndBookIdAndStatusIn(Long clientId, Long bookId, List<BorrowStatus> statuses);

    // بيستخدم لتحديث حالة أي سجل ACTIVE فات معاده لـ OVERDUE تلقائيًا
    List<BorrowRecord> findByStatusAndDueDateBefore(BorrowStatus status, java.time.LocalDate date);

    // بيستخدم في Review عشان يتحقق إن العميل استعار الكتاب ده قبل كده (أي حالة)
    boolean existsByClientIdAndBookId(Long clientId, Long bookId);

    long countByClientId(Long clientId);

    @Query("SELECT COALESCE(SUM(b.fineAmount), 0) FROM BorrowRecord b WHERE b.client.id = :clientId")
    Double sumFineAmountByClientId(Long clientId);
}
