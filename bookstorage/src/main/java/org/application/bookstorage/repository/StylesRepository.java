package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Styles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StylesRepository extends JpaRepository<Styles, Long> {
    /**
     * Найти жанр по точному названию (игнорируя регистр).
     *
     * @param name Название жанра.
     * @return Опциональный жанр.
     */
    Optional<Styles> findByNameIgnoreCase(String name);

    /**
     * Найти жанр, название которого содержит заданную строку (игнорируя регистр).
     *
     * @param name Подстрока для поиска в названии жанра.
     * @return Список жанров.
     */
    List<Styles> findByNameContainingIgnoreCase(String name);
    // Дополнительные методы поиска при необходимости
}
