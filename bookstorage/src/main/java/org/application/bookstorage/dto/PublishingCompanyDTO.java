package org.application.bookstorage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class PublishingCompanyDTO {
    @NotBlank(message = "Название издательства не может быть пустым")
    private String name;

    private int establishmentYear;

    private String contactInfo;

    private String city;
}
