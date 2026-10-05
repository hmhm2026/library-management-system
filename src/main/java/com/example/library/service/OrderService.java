package com.example.library.service;

import com.example.library.dto.OrderItemRequest;
import com.example.library.dto.OrderRequest;
import com.example.library.entity.*;
import com.example.library.exception.BusinessRuleException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.InventoryRepository;
import com.example.library.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * بيتحقق من كل عنصر في العربة (كتاب موجود + مخزون كافي) الأول قبل ما يخصم
     * أي حاجة. لو أي عنصر فشل، الطلب كله يترفض ومفيش أي تغيير بيتسجل
     * (العملية Transactional، فحتى لو خصمنا جزء وبعدين حصل خطأ، كل حاجة بترجع).
     */
    public Order createOrder(User client, OrderRequest request) {
        // الخطوة 1: تحقق من كل العناصر الأول (Book + Inventory + الكمية الكافية)
        List<Book> books = new ArrayList<>();
        List<Inventory> inventories = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Book book = bookRepository.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Book with id " + itemRequest.getBookId() + " was not found"));

            Inventory inventory = inventoryRepository.findByBookId(book.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No inventory record found for book id " + book.getId()));

            if (inventory.getAvailableCopies() < itemRequest.getQuantity()) {
                throw new BusinessRuleException(
                        "Insufficient stock for '" + book.getTitle() + "'. Available: " +
                                inventory.getAvailableCopies() + ", requested: " + itemRequest.getQuantity() + ".");
            }

            books.add(book);
            inventories.add(inventory);
        }

        // الخطوة 2: كل حاجة اتأكدت - دلوقتي نبني الطلب فعليًا ونخصم المخزون
        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.COMPLETED);

        double total = 0.0;
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemRequest = request.getItems().get(i);
            Book book = books.get(i);
            Inventory inventory = inventories.get(i);

            OrderItem item = new OrderItem();
            item.setBook(book);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(book.getPrice());
            item.setSubtotal(book.getPrice() * itemRequest.getQuantity());
            order.addItem(item);
            total += item.getSubtotal();

            inventory.setAvailableCopies(inventory.getAvailableCopies() - itemRequest.getQuantity());
            inventoryRepository.save(inventory);
        }
        order.setTotalPrice(total);

        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(Long clientId) {
        return orderRepository.findByClientId(clientId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id, User requester) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " was not found"));

        boolean isOwner = order.getClient().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("This order does not belong to you");
        }
        return order;
    }
}
