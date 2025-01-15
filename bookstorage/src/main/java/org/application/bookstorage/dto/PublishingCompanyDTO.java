package org.application.bookstorage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Data
public class PublishingCompanyDTO {
    @NotBlank(message = "Название издательства не может быть пустым")
    private String name;

    private LocalDate establishmentYear;

    private String contactInfo;

    private String city;
}
