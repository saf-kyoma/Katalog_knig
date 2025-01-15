package org.application.bookstorage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class AuthorDTO {
//    @NotNull(message = "ID автора не может быть null")
    private Integer id;

    @NotBlank(message = "ФИО автора не может быть пустым")
    private String fio;

    private String birthDate;

    private String country;

    private String nickname;
}


