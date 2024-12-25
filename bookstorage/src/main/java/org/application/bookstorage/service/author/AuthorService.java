package org.application.bookstorage.service.author;

import org.application.bookstorage.dao.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Author createAuthor(Author author);
    Optional<Author> getAuthorById(int id);
    List<Author> getAllAuthors();
    Author updateAuthor(int id, Author author);
    void deleteAuthor(int id);
    List<Author> searchAuthorsByFio(String fio);
}
