package org.application.bookstorage.controller.publishingcompany;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.PublishingCompany;
import org.application.bookstorage.dto.PublishingCompanyDTO;
import org.application.bookstorage.service.publishingcompany.PublishingCompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/publishing-companies")
@RequiredArgsConstructor
public class PublishingCompanyController {

    private final PublishingCompanyService publishingCompanyService;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(PublishingCompanyController.class);

    // Создание издательства
    @PostMapping
    public ResponseEntity<PublishingCompanyDTO> createPublishingCompany(@Valid @RequestBody PublishingCompanyDTO companyDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на создание издательства: {}", companyDTO);

        try {
            PublishingCompany company = mapToEntity(companyDTO);
            PublishingCompany createdCompany = publishingCompanyService.createPublishingCompany(company);

            // LOGGING ADDED
            logger.info("Издательство успешно создано: {}", createdCompany.getName());

            PublishingCompanyDTO responseDTO = mapToDTO(createdCompany);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при создании издательства: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение издательства по имени
    @GetMapping("/{name}")
    public ResponseEntity<PublishingCompanyDTO> getPublishingCompanyByName(@PathVariable String name) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение издательства по имени: {}", name);

        return publishingCompanyService.getPublishingCompanyByName(name)
                .map(company -> {
                    // LOGGING ADDED
                    logger.info("Издательство найдено: {}", company.getName());
                    return new ResponseEntity<>(mapToDTO(company), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    // LOGGING ADDED
                    logger.warn("Издательство с именем '{}' не найдено", name);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // Получение всех издательств
    @GetMapping
    public ResponseEntity<List<PublishingCompanyDTO>> getAllPublishingCompanies() {
        // LOGGING ADDED
        logger.info("Получен запрос на получение всех издательств");

        List<PublishingCompanyDTO> companies = publishingCompanyService.getAllPublishingCompanies()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("Возвращено {} издательств", companies.size());

        return new ResponseEntity<>(companies, HttpStatus.OK);
    }

    // Обновление издательства
    @PutMapping("/{originalName}")
    public ResponseEntity<PublishingCompanyDTO> updatePublishingCompany(
            @PathVariable String originalName,
            @Valid @RequestBody PublishingCompanyDTO companyDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на обновление издательства. Оригинальное имя={}, новые данные={}",
                originalName, companyDTO);

        try {
            PublishingCompany companyDetails = mapToEntity(companyDTO);
            PublishingCompany updatedCompany = publishingCompanyService.updatePublishingCompany(originalName, companyDetails);

            // LOGGING ADDED
            logger.info("Издательство '{}' успешно обновлено. Текущее имя='{}'",
                    originalName, updatedCompany.getName());

            PublishingCompanyDTO responseDTO = mapToDTO(updatedCompany);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при обновлении издательства '{}': {}", originalName, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление издательств (bulk-delete)
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deletePublishingCompanies(@RequestBody List<String> names) {
        // LOGGING ADDED
        logger.info("Получен запрос на массовое удаление издательств: {}", names);

        try {
            publishingCompanyService.deletePublishingCompanies(names);

            // LOGGING ADDED
            logger.info("Массовое удаление издательств завершено.");

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при массовом удалении издательств: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Эндпоинт для поиска издательств по части названия
    @GetMapping("/search")
    public ResponseEntity<List<PublishingCompany>> searchPublishingCompanies(@RequestParam("q") String query) {
        // LOGGING ADDED
        logger.info("Получен запрос на поиск издательств по части названия: {}", query);

        List<PublishingCompany> companies = publishingCompanyService.searchPublishingCompaniesByName(query);

        // LOGGING ADDED
        logger.info("Поиск завершён. Найдено {} совпадений", companies.size());

        return ResponseEntity.ok(companies);
    }

    // Ручной маппинг DTO в сущность
    private PublishingCompany mapToEntity(PublishingCompanyDTO dto) {
        PublishingCompany company = new PublishingCompany();
        company.setName(dto.getName());
        company.setEstablishmentYear(dto.getEstablishmentYear());
        company.setContactInfo(dto.getContactInfo());
        company.setCity(dto.getCity());
        return company;
    }

    // Ручной маппинг сущности в DTO
    private PublishingCompanyDTO mapToDTO(PublishingCompany company) {
        PublishingCompanyDTO dto = new PublishingCompanyDTO();
        dto.setName(company.getName());
        dto.setEstablishmentYear(company.getEstablishmentYear());
        dto.setContactInfo(company.getContactInfo());
        dto.setCity(company.getCity());
        return dto;
    }
}
