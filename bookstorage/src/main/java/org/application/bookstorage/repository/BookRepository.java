package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Book;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    List<Book> findByNameContainingIgnoreCase(String name, Sort sort);
    // Дополнительные методы поиска при необходимости
}

