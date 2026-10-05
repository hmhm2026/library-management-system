package com.example.library.repository;

import com.example.library.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // بيستخدم في Review عشان يتحقق إن العميل اشترى الكتاب ده قبل كده
    boolean existsByOrder_ClientIdAndBookId(Long clientId, Long bookId);
}
