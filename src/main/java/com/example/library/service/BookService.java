package com.example.library.service;

import com.example.library.dto.BookRequest;
import com.example.library.entity.Book;
import com.example.library.entity.Category;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " was not found"));
    }

    public Book createBook(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);

        // isbn بيتحدد تلقائيًا هنا: أعلى رقم موجود + 1 (يبدأ من 1 لو الجدول فاضي)
        Long nextIsbn = bookRepository.findMaxIsbn() + 1;
        book.setIsbn(nextIsbn);

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookRequest request) {
        Book existing = getBookById(id);
        applyRequest(existing, request);
        // isbn ثابت من الإنشاء ومبيتغيرش أبدًا
        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        Book existing = getBookById(id);
        bookRepository.delete(existing);
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setGenre(request.getGenre());
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category with id " + request.getCategoryId() + " was not found"));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }
}
