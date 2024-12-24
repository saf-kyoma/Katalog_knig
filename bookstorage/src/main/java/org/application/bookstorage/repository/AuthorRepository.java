package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    // Дополнительные методы поиска при необходимости
}

