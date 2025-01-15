package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    // Метод для поиска авторов по частичному совпадению ФИО, игнорируя регистр
    List<Author> findByFioContainingIgnoreCase(String fio);
    // Метод для поиска авторов по частичному совпадению ФИО или псевдонима, игнорируя регистр
    List<Author> findByFioContainingIgnoreCaseOrNicknameContainingIgnoreCase(String fio, String nickname);

    // Дополнительные методы поиска при необходимости
}

