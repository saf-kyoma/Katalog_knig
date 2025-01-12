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

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class PublishingCompanyServiceImpl implements PublishingCompanyService {

    private final PublishingCompanyRepository publishingCompanyRepository;
    private final BookRepository bookRepository;

    // LOGGING ADDED: Инициализация логгера для этого сервиса
    private static final Logger logger = LoggerFactory.getLogger(PublishingCompanyServiceImpl.class);

    @Override
    public PublishingCompany createPublishingCompany(PublishingCompany company) {
        logger.info("Создание издательства: {}", company);
        PublishingCompany savedCompany = publishingCompanyRepository.save(company);
        logger.info("Издательство успешно создано: {}", savedCompany.getName());
        return savedCompany;
    }

    @Override
    public Optional<PublishingCompany> getPublishingCompanyByName(String name) {
        logger.info("Получение издательства по имени: {}", name);
        Optional<PublishingCompany> companyOpt = publishingCompanyRepository.findById(name);
        if (companyOpt.isPresent()) {
            logger.info("Издательство найдено: {}", name);
        } else {
            logger.warn("Издательство с именем {} не найдено", name);
        }
        return companyOpt;
    }

    @Override
    public List<PublishingCompany> getAllPublishingCompanies() {
        logger.info("Получение всех издательств");
        List<PublishingCompany> companies = publishingCompanyRepository.findAll();
        logger.info("Найдено {} издательств", companies.size());
        return companies;
    }

    @Override
    public PublishingCompany updatePublishingCompany(String originalName, PublishingCompany updatedCompany) {
        logger.info("Обновление издательства. Оригинальное имя: {}, новые данные: {}", originalName, updatedCompany);
        // Находим исходное издательство по старому названию
        PublishingCompany oldCompany = publishingCompanyRepository.findById(originalName)
                .orElseThrow(() -> {
                    logger.error("Издательство не найдено с именем {}", originalName);
                    return new RuntimeException("Издательство не найдено с именем " + originalName);
                });

        String newName = updatedCompany.getName();

        if (!originalName.equals(newName)) {
            // Проверяем, что новое имя не занято
            if (publishingCompanyRepository.existsById(newName)) {
                logger.error("Издательство с названием {} уже существует", newName);
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
                logger.debug("Обновлена связь книги (ISBN={}) на новое издательство {}", book.getIsbn(), newName);
            }

            // Сохраняем новое издательство
            publishingCompanyRepository.save(newCompany);
            logger.info("Новое издательство {} сохранено", newName);

            // Удаляем старое издательство
            publishingCompanyRepository.delete(oldCompany);
            logger.info("Старое издательство {} удалено", originalName);

            return newCompany;
        } else {
            // Если название не меняется, просто обновляем остальные поля
            oldCompany.setEstablishmentYear(updatedCompany.getEstablishmentYear());
            oldCompany.setContactInfo(updatedCompany.getContactInfo());
            oldCompany.setCity(updatedCompany.getCity());
            PublishingCompany savedOld = publishingCompanyRepository.save(oldCompany);
            logger.info("Издательство {} обновлено (без смены имени)", originalName);
            return savedOld;
        }
    }

    @Override
    public void deletePublishingCompanies(List<String> names) {
        logger.info("Массовое удаление издательств. Список имён: {}", names);
        List<PublishingCompany> companies = publishingCompanyRepository.findAllById(names);
        if (companies.size() != names.size()) {
            logger.error("Некоторые издательства не найдены для удаления. Ожидается {} , найдено {}", names.size(), companies.size());
            throw new RuntimeException("Некоторые издательства не найдены для удаления.");
        }
        publishingCompanyRepository.deleteAll(companies);
        logger.info("Издательства успешно удалены: {}", names);
    }

    @Override
    public List<PublishingCompany> searchPublishingCompaniesByName(String name) {
        logger.info("Поиск издательств, содержащих: {}", name);
        List<PublishingCompany> result = publishingCompanyRepository.findByNameContainingIgnoreCase(name);
        logger.info("Найдено {} издательств по запросу '{}'", result.size(), name);
        return result;
    }
}
