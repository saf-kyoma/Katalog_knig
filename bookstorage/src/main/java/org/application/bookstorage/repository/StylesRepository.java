package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Styles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StylesRepository extends JpaRepository<Styles, Long> {
    // Дополнительные методы поиска при необходимости
}
