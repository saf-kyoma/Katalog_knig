package org.application.bookstorage.controller.authorship;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dto.AuthorshipDTO;
import org.application.bookstorage.service.authorship.AuthorshipService;
import org.application.bookstorage.service.author.AuthorService;
import org.application.bookstorage.service.book.BookService;
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
@RequestMapping("/api/authorships")
@RequiredArgsConstructor
public class AuthorshipController {

    private final AuthorshipService authorshipService;
    private final BookService bookService;
    private final AuthorService authorService;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(AuthorshipController.class);

    // Создание авторства
    @PostMapping
    public ResponseEntity<AuthorshipDTO> createAuthorship(@Valid @RequestBody AuthorshipDTO authorshipDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на создание авторства: {}", authorshipDTO);

        try {
            // Проверка существования книги и автора
            Book book = bookService.getBookByIsbn(authorshipDTO.getBookIsbn())
                    .orElseThrow(() -> new RuntimeException("Книга не найдена: " + authorshipDTO.getBookIsbn()));
            org.application.bookstorage.dao.Author author = authorService.getAuthorById(authorshipDTO.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("Автор не найден: " + authorshipDTO.getAuthorId()));

            // Создание объекта Authorship
            Authorship authorship = new Authorship();
            AuthorshipId id = new AuthorshipId(authorshipDTO.getBookIsbn(), authorshipDTO.getAuthorId());
            authorship.setId(id);
            authorship.setBook(book);
            authorship.setAuthor(author);

            Authorship createdAuthorship = authorshipService.createAuthorship(authorship);

            // LOGGING ADDED
            logger.info("Авторство успешно создано: {}", id);

            AuthorshipDTO responseDTO = mapToDTO(createdAuthorship);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при создании авторства: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение авторства по ключу
    @GetMapping("/{bookIsbn}/{authorId}")
    public ResponseEntity<AuthorshipDTO> getAuthorshipById(@PathVariable String bookIsbn, @PathVariable int authorId) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение авторства по ключу: bookIsbn={}, authorId={}", bookIsbn, authorId);

        AuthorshipId id = new AuthorshipId(bookIsbn, authorId);
        return authorshipService.getAuthorshipById(id)
                .map(authorship -> {
                    // LOGGING ADDED
                    logger.info("Авторство найдено: {}", id);
                    return new ResponseEntity<>(mapToDTO(authorship), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    // LOGGING ADDED
                    logger.warn("Авторство не найдено: {}", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // Получение всех авторств
    @GetMapping
    public ResponseEntity<List<AuthorshipDTO>> getAllAuthorships() {
        // LOGGING ADDED
        logger.info("Получен запрос на получение всех авторств");

        List<AuthorshipDTO> authorships = authorshipService.getAllAuthorships()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("Возвращено {} авторств", authorships.size());

        return new ResponseEntity<>(authorships, HttpStatus.OK);
    }

    // Обновление авторства
    @PutMapping("/{bookIsbn}/{authorId}")
    public ResponseEntity<AuthorshipDTO> updateAuthorship(@PathVariable String bookIsbn, @PathVariable int authorId, @Valid @RequestBody AuthorshipDTO authorshipDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на обновление авторства bookIsbn={}, authorId={}. Новые данные: {}",
                bookIsbn, authorId, authorshipDTO);

        try {
            // Проверка существования новой книги и нового автора, если они изменяются
            Book newBook = bookService.getBookByIsbn(authorshipDTO.getBookIsbn())
                    .orElseThrow(() -> new RuntimeException("Книга не найдена: " + authorshipDTO.getBookIsbn()));
            org.application.bookstorage.dao.Author newAuthor = authorService.getAuthorById(authorshipDTO.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("Автор не найден: " + authorshipDTO.getAuthorId()));

            // Создание объекта Authorship с новыми данными
            Authorship authorshipDetails = new Authorship();
            AuthorshipId newId = new AuthorshipId(authorshipDTO.getBookIsbn(), authorshipDTO.getAuthorId());
            authorshipDetails.setId(newId);
            authorshipDetails.setBook(newBook);
            authorshipDetails.setAuthor(newAuthor);

            // Обновление авторства
            Authorship updatedAuthorship = authorshipService.updateAuthorship(new AuthorshipId(bookIsbn, authorId), authorshipDetails);

            // LOGGING ADDED
            logger.info("Авторство успешно обновлено: старый ключ={}, новый ключ={}",
                    new AuthorshipId(bookIsbn, authorId), newId);

            AuthorshipDTO responseDTO = mapToDTO(updatedAuthorship);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при обновлении авторства: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление авторства
    @DeleteMapping("/{bookIsbn}/{authorId}")
    public ResponseEntity<Void> deleteAuthorship(@PathVariable String bookIsbn, @PathVariable int authorId) {
        // LOGGING ADDED
        logger.info("Получен запрос на удаление авторства: bookIsbn={}, authorId={}", bookIsbn, authorId);

        try {
            AuthorshipId id = new AuthorshipId(bookIsbn, authorId);
            authorshipService.deleteAuthorship(id);

            // LOGGING ADDED
            logger.info("Авторство удалено: {}", id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при удалении авторства: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Ручной маппинг сущности в DTO
    private AuthorshipDTO mapToDTO(Authorship authorship) {
        AuthorshipDTO dto = new AuthorshipDTO();
        dto.setBookIsbn(authorship.getBook().getIsbn());
        dto.setAuthorId(authorship.getAuthor().getId());
        return dto;
    }
}
