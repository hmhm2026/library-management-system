package com.example.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * الطلب (عربة تسوق) - بيحتوي أكتر من OrderItem، كل عنصر كتاب + كمية.
 * totalPrice بيتحسب تلقائيًا = مجموع subtotal بتاع كل العناصر، مينفعش يتبعت من العميل.
 * الشراء ذاتي خدمة وفوري: بمجرد إنشاء الطلب بينجح، الحالة تبقى COMPLETED مباشرة
 * (مفيش خطوة دفع منفصلة في النطاق الحالي).
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** بيسهّل ربط العنصر بالطلب من الاتجاهين مرة واحدة. */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
