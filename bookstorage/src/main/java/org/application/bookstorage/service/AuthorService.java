package org.application.bookstorage.service;

import org.application.bookstorage.entity.Author;
import org.application.bookstorage.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    // Получить всех авторов
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // Получить автора по ID
    public Author getAuthorById(Integer id) {
        return authorRepository.findById(id).orElse(null);
    }

    // Добавить автора
    public Author addAuthor(Author author) {
        return authorRepository.save(author);
    }

    // Обновить автора
    public Author updateAuthor(Author author) {
        return authorRepository.save(author);
    }

    // Удалить автора
    public void deleteAuthor(Integer id) {
        authorRepository.deleteById(id);
    }
}
