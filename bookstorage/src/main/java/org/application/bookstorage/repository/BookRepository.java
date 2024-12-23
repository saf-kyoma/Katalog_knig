package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    // Дополнительные методы поиска при необходимости
}

