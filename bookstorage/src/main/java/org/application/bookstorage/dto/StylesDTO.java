package org.application.bookstorage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class StylesDTO {
    private Long id;

    @NotBlank(message = "Название стиля не может быть пустым")
    private String name;
}
