package org.application.bookstorage.service.book;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.repository.BookRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book updateBook(String isbn, Book bookDetails) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Книга не найдена с ISBN " + isbn));
        book.setName(bookDetails.getName());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setAgeLimit(bookDetails.getAgeLimit());
        book.setPublishingCompany(bookDetails.getPublishingCompany());
        book.setPageCount(bookDetails.getPageCount());
        book.setLanguage(bookDetails.getLanguage());
        book.setCost(bookDetails.getCost());
        book.setCountOfBooks(bookDetails.getCountOfBooks());
        // Обновление других полей при необходимости
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Книга не найдена с ISBN " + isbn));
        bookRepository.delete(book);
    }
}

