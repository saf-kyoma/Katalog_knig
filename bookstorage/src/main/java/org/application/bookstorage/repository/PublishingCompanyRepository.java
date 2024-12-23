package org.application.bookstorage.repository;

import org.application.bookstorage.dao.PublishingCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublishingCompanyRepository extends JpaRepository<PublishingCompany, String> {
    // Дополнительные методы поиска при необходимости
}

