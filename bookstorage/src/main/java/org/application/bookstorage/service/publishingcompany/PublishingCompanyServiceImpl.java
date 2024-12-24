package org.application.bookstorage.service.publishingcompany;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.PublishingCompany;
import org.application.bookstorage.repository.PublishingCompanyRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PublishingCompanyServiceImpl implements PublishingCompanyService {

    private final PublishingCompanyRepository publishingCompanyRepository;

    @Override
    public PublishingCompany createPublishingCompany(PublishingCompany company) {
        return publishingCompanyRepository.save(company);
    }

    @Override
    public Optional<PublishingCompany> getPublishingCompanyByName(String name) {
        return publishingCompanyRepository.findById(name);
    }

    @Override
    public List<PublishingCompany> getAllPublishingCompanies() {
        return publishingCompanyRepository.findAll();
    }

    @Override
    public PublishingCompany updatePublishingCompany(String name, PublishingCompany companyDetails) {
        PublishingCompany company = publishingCompanyRepository.findById(name)
                .orElseThrow(() -> new RuntimeException("Издательство не найдено с именем " + name));
        company.setEstablishmentYear(companyDetails.getEstablishmentYear());
        company.setContactInfo(companyDetails.getContactInfo());
        company.setCity(companyDetails.getCity());
        // Обновление других полей при необходимости
        return publishingCompanyRepository.save(company);
    }

    @Override
    public void deletePublishingCompany(String name) {
        PublishingCompany company = publishingCompanyRepository.findById(name)
                .orElseThrow(() -> new RuntimeException("Издательство не найдено с именем " + name));
        publishingCompanyRepository.delete(company);
    }
}
