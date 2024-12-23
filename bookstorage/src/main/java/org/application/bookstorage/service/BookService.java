package org.application.bookstorage.service;

import org.application.bookstorage.entity.Book;
import org.application.bookstorage.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Получить все книги
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Получить книгу по ISBN
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn).orElse(null);
    }

    // Добавить книгу
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    // Обновить книгу
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    // Удалить книгу
    public void deleteBook(String isbn) {
        bookRepository.deleteById(isbn);
    }
}
