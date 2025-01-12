package org.application.bookstorage.service.publishingcompany;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.PublishingCompany;
import org.application.bookstorage.repository.BookRepository;
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
    private final BookRepository bookRepository;

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
    public void deletePublishingCompanies(List<String> names) {
        // 1) Находим все издательства по списку
        List<PublishingCompany> companies = publishingCompanyRepository.findAllById(names);
        if (companies.size() != names.size()) {
            // Если вдруг какое-то издательство не найдено
            throw new RuntimeException("Некоторые издательства не найдены для удаления.");
        }

        // 2) Удаляем издательства
        //    Так как в PublishingCompany есть:
        //    @OneToMany(mappedBy = "publishingCompany", cascade = CascadeType.ALL, orphanRemoval = true)
        //    удалятся и все книги, связанные с этим издательством.
        publishingCompanyRepository.deleteAll(companies);

        // Всё. Благодаря cascade=ALL и orphanRemoval=true в PublishingCompany
        // все связанные книги автоматически удалятся из Book.
    }

    @Override
    public List<PublishingCompany> searchPublishingCompaniesByName(String name) {
        return publishingCompanyRepository.findByNameContainingIgnoreCase(name);
    }
}
