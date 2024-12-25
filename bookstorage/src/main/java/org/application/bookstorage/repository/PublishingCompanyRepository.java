package org.application.bookstorage.repository;

import org.application.bookstorage.dao.PublishingCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublishingCompanyRepository extends JpaRepository<PublishingCompany, String> {

    Optional<PublishingCompany> findByName(String name);

    void deleteByName(String name);
    // Дополнительные методы поиска при необходимости
}

