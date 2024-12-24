package org.application.bookstorage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class BookDTO {
    @NotBlank(message = "ISBN книги не может быть пустым")
    private String isbn;

    @NotBlank(message = "Название книги не может быть пустым")
    private String name;

    private String publicationYear;

    private float ageLimit;

    @NotBlank(message = "Название издательства не может быть пустым")
    private String publishingCompany;

    private int pageCount;

    private String language;

    private float cost;

    private int countOfBooks;

    private List<AuthorDTO> authors;
}
