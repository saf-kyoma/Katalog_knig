package org.application.bookstorage.service.book;

import org.application.bookstorage.dao.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {
    Book createBook(Book book);
    Optional<Book> getBookByIsbn(String isbn);
    List<Book> getAllBooks(String search, String sortColumn, String sortOrder);
    Book updateBook(String isbn, Book book);
    void deleteBook(String isbn);

    // Новый метод для массового удаления
    void deleteBooks(List<String> isbns);
}
