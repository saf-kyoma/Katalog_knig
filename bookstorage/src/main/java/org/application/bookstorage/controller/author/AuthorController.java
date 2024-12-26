package org.application.bookstorage.controller.author;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Author;
import org.application.bookstorage.dto.AuthorDTO;
import org.application.bookstorage.service.author.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    // Создание автора
    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(@Valid @RequestBody AuthorDTO authorDTO) {
        Author author = mapToEntity(authorDTO);
        Author createdAuthor = authorService.createAuthor(author);
        AuthorDTO responseDTO = mapToDTO(createdAuthor);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Получение автора по ID
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable int id) {
        return authorService.getAuthorById(id)
                .map(author -> new ResponseEntity<>(mapToDTO(author), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Получение всех авторов с поддержкой сортировки
    @GetMapping
    public ResponseEntity<List<AuthorDTO>> getAllAuthors(
            @RequestParam(required = false, name = "sort_column") String sortColumn,
            @RequestParam(required = false, name = "sort_order") String sortOrder) {
        List<Author> authors = authorService.getAllAuthors();

        // Применяем сортировку, если она задана
        if (sortColumn != null && sortOrder != null) {
            Comparator<Author> comparator = getComparator(sortColumn);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            authors = authors.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        List<AuthorDTO> authorDTOs = authors.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(authorDTOs, HttpStatus.OK);
    }

    // Добавление метода поиска авторов
    @GetMapping("/search")
    public ResponseEntity<List<AuthorDTO>> searchAuthors(
            @RequestParam("q") String query,
            @RequestParam(required = false, name = "sort_column") String sortColumn,
            @RequestParam(required = false, name = "sort_order") String sortOrder) {
        List<Author> authors = authorService.searchAuthors(query);

        // Применяем сортировку, если она задана
        if (sortColumn != null && sortOrder != null) {
            Comparator<Author> comparator = getComparator(sortColumn);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            authors = authors.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        List<AuthorDTO> authorDTOs = authors.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(authorDTOs, HttpStatus.OK);
    }

    // Обновление автора
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable int id, @Valid @RequestBody AuthorDTO authorDTO) {
        try {
            Author authorDetails = mapToEntity(authorDTO);
            Author updatedAuthor = authorService.updateAuthor(id, authorDetails);
            AuthorDTO responseDTO = mapToDTO(updatedAuthor);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление автора
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable int id) {
        try {
            authorService.deleteAuthor(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Ручной маппинг DTO в сущность
    private Author mapToEntity(AuthorDTO dto) {
        Author author = new Author();
        author.setId(dto.getId());
        author.setFio(dto.getFio());
        author.setBirthDate(dto.getBirthDate());
        author.setCountry(dto.getCountry());
        author.setNickname(dto.getNickname());
        return author;
    }

    // Ручной маппинг сущности в DTO
    private AuthorDTO mapToDTO(Author author) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setFio(author.getFio());
        dto.setBirthDate(author.getBirthDate());
        dto.setCountry(author.getCountry());
        dto.setNickname(author.getNickname());
        return dto;
    }

    /**
     * Возвращает компаратор для сортировки авторов по заданному столбцу.
     */
    private Comparator<Author> getComparator(String sortColumn) {
        switch (sortColumn.toLowerCase()) {
            case "fio":
                return Comparator.comparing(Author::getFio, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "birthdate":
                return Comparator.comparing(Author::getBirthDate, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "country":
                return Comparator.comparing(Author::getCountry, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "nickname":
                return Comparator.comparing(Author::getNickname, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            default:
                return Comparator.comparing(Author::getFio, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }
    }
}
