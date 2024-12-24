package org.application.bookstorage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class BookStylesDTO {
    @NotBlank(message = "ISBN книги не может быть пустым")
    private String bookIsbn;

    @NotNull(message = "ID стиля не может быть null")
    private Long styleId;
}
