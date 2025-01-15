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

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    // Создание автора
    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(@Valid @RequestBody AuthorDTO authorDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на создание автора: {}", authorDTO);

        Author author = mapToEntity(authorDTO);
        Author createdAuthor = authorService.createAuthor(author);

        // LOGGING ADDED
        logger.info("Автор успешно создан с ID: {}", createdAuthor.getId());

        AuthorDTO responseDTO = mapToDTO(createdAuthor);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Получение автора по ID
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable int id) {
        // LOGGING ADDED
        logger.info("Получен запрос на поиск автора по ID: {}", id);

        return authorService.getAuthorById(id)
                .map(author -> {
                    // LOGGING ADDED
                    logger.info("Автор найден: {}", author.getId());
                    return new ResponseEntity<>(mapToDTO(author), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    // LOGGING ADDED
                    logger.warn("Автор с ID {} не найден", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // Получение всех авторов с поддержкой сортировки
    @GetMapping
    public ResponseEntity<List<AuthorDTO>> getAllAuthors(
            @RequestParam(required = false, name = "sort_column") String sortColumn,
            @RequestParam(required = false, name = "sort_order") String sortOrder) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение всех авторов с параметрами сортировки: sort_column={}, sort_order={}",
                sortColumn, sortOrder);

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

            // LOGGING ADDED
            logger.info("Список авторов отсортирован по столбцу '{}' в порядке '{}'", sortColumn, sortOrder);
        }

        List<AuthorDTO> authorDTOs = authors.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("Возвращено {} авторов", authorDTOs.size());

        return new ResponseEntity<>(authorDTOs, HttpStatus.OK);
    }

    // Добавление метода поиска авторов
    @GetMapping("/search")
    public ResponseEntity<List<AuthorDTO>> searchAuthors(
            @RequestParam("q") String query,
            @RequestParam(required = false, name = "sort_column") String sortColumn,
            @RequestParam(required = false, name = "sort_order") String sortOrder) {
        // LOGGING ADDED
        logger.info("Получен запрос на поиск авторов. Запрос: {}, sort_column={}, sort_order={}",
                query, sortColumn, sortOrder);

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

            // LOGGING ADDED
            logger.info("Список найденных авторов отсортирован по столбцу '{}' в порядке '{}'", sortColumn, sortOrder);
        }

        List<AuthorDTO> authorDTOs = authors.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("По запросу '{}' найдено {} авторов", query, authorDTOs.size());

        return new ResponseEntity<>(authorDTOs, HttpStatus.OK);
    }

    // Обновление автора
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable int id, @Valid @RequestBody AuthorDTO authorDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на обновление автора с ID {}: {}", id, authorDTO);

        try {
            Author authorDetails = mapToEntity(authorDTO);
            Author updatedAuthor = authorService.updateAuthor(id, authorDetails);

            // LOGGING ADDED
            logger.info("Автор с ID {} успешно обновлён", id);

            AuthorDTO responseDTO = mapToDTO(updatedAuthor);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при обновлении автора с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление автора
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable int id) {
        // LOGGING ADDED
        logger.info("Получен запрос на удаление автора с ID {}", id);

        try {
            authorService.deleteAuthor(id);

            // LOGGING ADDED
            logger.info("Автор с ID {} успешно удалён", id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при удалении автора с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deleteAuthors(
            @RequestBody List<Integer> authorIds,
            @RequestParam(name = "removeEverything", required = false, defaultValue = "false") boolean removeEverything
    ) {
        // LOGGING ADDED
        logger.info("Получен запрос на массовое удаление авторов. authorIds={}, removeEverything={}", authorIds, removeEverything);

        try {
            // Вызываем сервисный метод
            authorService.deleteAuthors(authorIds, removeEverything);

            // LOGGING ADDED
            logger.info("Массовое удаление авторов завершено. authorIds={}", authorIds);

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при массовом удалении авторов: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
