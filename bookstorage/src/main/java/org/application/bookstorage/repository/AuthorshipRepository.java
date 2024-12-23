package org.application.bookstorage.repository;

import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorshipRepository extends JpaRepository<Authorship, AuthorshipId> {
    // Дополнительные методы поиска при необходимости
}
