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

@RestController
@RequestMapping("/api/publishing-companies")
@RequiredArgsConstructor
public class PublishingCompanyController {

    private final PublishingCompanyService publishingCompanyService;

    // Создание издательства
    @PostMapping
    public ResponseEntity<PublishingCompanyDTO> createPublishingCompany(@Valid @RequestBody PublishingCompanyDTO companyDTO) {
        try {
            PublishingCompany company = mapToEntity(companyDTO);
            PublishingCompany createdCompany = publishingCompanyService.createPublishingCompany(company);
            PublishingCompanyDTO responseDTO = mapToDTO(createdCompany);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение издательства по имени
    @GetMapping("/{name}")
    public ResponseEntity<PublishingCompanyDTO> getPublishingCompanyByName(@PathVariable String name) {
        return publishingCompanyService.getPublishingCompanyByName(name)
                .map(company -> new ResponseEntity<>(mapToDTO(company), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Получение всех издательств
    @GetMapping
    public ResponseEntity<List<PublishingCompanyDTO>> getAllPublishingCompanies() {
        List<PublishingCompanyDTO> companies = publishingCompanyService.getAllPublishingCompanies()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }

    // Обновление издательства
    @PutMapping("/{name}")
    public ResponseEntity<PublishingCompanyDTO> updatePublishingCompany(@PathVariable String name, @Valid @RequestBody PublishingCompanyDTO companyDTO) {
        try {
            PublishingCompany companyDetails = mapToEntity(companyDTO);
            PublishingCompany updatedCompany = publishingCompanyService.updatePublishingCompany(name, companyDetails);
            PublishingCompanyDTO responseDTO = mapToDTO(updatedCompany);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление издательства
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deletePublishingCompanies(@RequestBody List<String> names) {
        try {
            publishingCompanyService.deletePublishingCompanies(names);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Если возникли проблемы (какое-то издательство не найдено и т.д.)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }




    // Эндпоинт для поиска издательств по части названия
    @GetMapping("/search")
    public ResponseEntity<List<PublishingCompany>> searchPublishingCompanies(@RequestParam("q") String query) {
        List<PublishingCompany> companies = publishingCompanyService.searchPublishingCompaniesByName(query);
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
