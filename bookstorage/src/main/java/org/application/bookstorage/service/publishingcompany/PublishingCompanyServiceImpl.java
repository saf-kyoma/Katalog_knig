package org.application.bookstorage.service.publishingcompany;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Book;
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
    public PublishingCompany updatePublishingCompany(String originalName, PublishingCompany updatedCompany) {
        // Находим исходное издательство по старому названию
        PublishingCompany oldCompany = publishingCompanyRepository.findById(originalName)
                .orElseThrow(() -> new RuntimeException("Издательство не найдено с именем " + originalName));

        String newName = updatedCompany.getName();

        if (!originalName.equals(newName)) {
            // Проверяем, что новое имя не занято
            if (publishingCompanyRepository.existsById(newName)) {
                throw new RuntimeException("Издательство с названием " + newName + " уже существует.");
            }

            // Создаём новое издательство
            PublishingCompany newCompany = new PublishingCompany();
            newCompany.setName(newName);
            newCompany.setEstablishmentYear(updatedCompany.getEstablishmentYear());
            newCompany.setContactInfo(updatedCompany.getContactInfo());
            newCompany.setCity(updatedCompany.getCity());
            newCompany.setBooks(oldCompany.getBooks());

            // Обновляем связь в книгах
            for (Book book : oldCompany.getBooks()) {
                book.setPublishingCompany(newCompany);
            }

            // Сохраняем новое издательство
            publishingCompanyRepository.save(newCompany);

            // Удаляем старое издательство
            publishingCompanyRepository.delete(oldCompany);

            return newCompany;
        } else {
            // Если название не меняется, просто обновляем остальные поля
            oldCompany.setEstablishmentYear(updatedCompany.getEstablishmentYear());
            oldCompany.setContactInfo(updatedCompany.getContactInfo());
            oldCompany.setCity(updatedCompany.getCity());
            return publishingCompanyRepository.save(oldCompany);
        }
    }

    @Override
    public void deletePublishingCompanies(List<String> names) {
        List<PublishingCompany> companies = publishingCompanyRepository.findAllById(names);
        if (companies.size() != names.size()) {
            throw new RuntimeException("Некоторые издательства не найдены для удаления.");
        }
        publishingCompanyRepository.deleteAll(companies);
    }

    @Override
    public List<PublishingCompany> searchPublishingCompaniesByName(String name) {
        return publishingCompanyRepository.findByNameContainingIgnoreCase(name);
    }
}
