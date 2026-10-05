package com.example.library.service;

import com.example.library.dto.InventoryRequest;
import com.example.library.entity.Book;
import com.example.library.entity.Inventory;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BookRepository bookRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record with id " + id + " was not found"));
    }

    public Inventory getInventoryByBookId(Long bookId) {
        return inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory record found for book id " + bookId));
    }

    public Inventory createInventory(InventoryRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book with id " + request.getBookId() + " was not found"));

        if (inventoryRepository.existsByBookId(request.getBookId())) {
            throw new DuplicateResourceException(
                    "Inventory record already exists for book id " + request.getBookId() +
                            ". Use PUT /api/inventory/{id} to update the existing record instead.");
        }

        Inventory inventory = new Inventory();
        inventory.setBook(book);
        inventory.setAvailableCopies(request.getAvailableCopies());
        return inventoryRepository.save(inventory);
    }

    public Inventory updateInventory(Long id, Integer availableCopies) {
        Inventory existing = getInventoryById(id);
        existing.setAvailableCopies(availableCopies);
        return inventoryRepository.save(existing);
    }

    public void deleteInventory(Long id) {
        Inventory existing = getInventoryById(id);
        inventoryRepository.delete(existing);
    }
}
