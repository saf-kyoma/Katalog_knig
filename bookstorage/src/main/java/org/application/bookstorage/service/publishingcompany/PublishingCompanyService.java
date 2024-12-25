package org.application.bookstorage.service.publishingcompany;

import org.application.bookstorage.dao.PublishingCompany;

import java.util.List;
import java.util.Optional;

public interface PublishingCompanyService {
    PublishingCompany createPublishingCompany(PublishingCompany company);
    Optional<PublishingCompany> getPublishingCompanyByName(String name);
    List<PublishingCompany> getAllPublishingCompanies();
    PublishingCompany updatePublishingCompany(String name, PublishingCompany company);
    void deletePublishingCompany(String name);
    List<PublishingCompany> searchPublishingCompaniesByName(String name);
}
