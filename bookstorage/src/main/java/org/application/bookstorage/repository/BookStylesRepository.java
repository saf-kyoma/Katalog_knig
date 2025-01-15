package org.application.bookstorage.repository;

import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookStylesRepository extends JpaRepository<BookStyles, BookStylesId> {
    // Дополнительные методы поиска при необходимости
}
