package org.application.bookstorage.controller.bookstyles;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.application.bookstorage.dto.BookStylesDTO;
import org.application.bookstorage.service.book.BookService;
import org.application.bookstorage.service.bookstyles.BookStylesService;
import org.application.bookstorage.service.styles.StylesService;
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
@RequestMapping("/api/book-styles")
@RequiredArgsConstructor
public class BookStylesController {

    private final BookStylesService bookStylesService;
    private final BookService bookService;
    private final StylesService stylesService;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(BookStylesController.class);

    // Создание связи книги со стилем
    @PostMapping
    public ResponseEntity<BookStylesDTO> createBookStyles(@Valid @RequestBody BookStylesDTO bookStylesDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на создание связи книги со стилем: {}", bookStylesDTO);

        try {
            BookStyles bookStyles = mapToEntity(bookStylesDTO);
            BookStylesId id = new BookStylesId(bookStylesDTO.getBookIsbn(), bookStylesDTO.getStyleId());

            // Проверка существования книги
            bookStyles.setBook(bookService.getBookByIsbn(bookStylesDTO.getBookIsbn())
                    .orElseThrow(() -> new RuntimeException("Книга не найдена: " + bookStylesDTO.getBookIsbn())));

            // Проверка существования стиля
            bookStyles.setStyleEntity(stylesService.getStyleById(bookStylesDTO.getStyleId())
                    .orElseThrow(() -> new RuntimeException("Стиль не найден: " + bookStylesDTO.getStyleId())));

            // Создание связи
            BookStyles createdBookStyles = bookStylesService.createBookStyles(bookStyles);

            // LOGGING ADDED
            logger.info("Связь книги со стилем успешно создана: {}", id);

            BookStylesDTO responseDTO = mapToDTO(createdBookStyles);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при создании связи книги со стилем: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение связи книги со стилем по ключу
    @GetMapping("/{bookIsbn}/{styleId}")
    public ResponseEntity<BookStylesDTO> getBookStylesById(@PathVariable String bookIsbn, @PathVariable Long styleId) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение связи книги со стилем: bookIsbn={}, styleId={}", bookIsbn, styleId);

        BookStylesId id = new BookStylesId(bookIsbn, styleId);
        return bookStylesService.getBookStylesById(id)
                .map(bookStyles -> {
                    // LOGGING ADDED
                    logger.info("Связь книги со стилем найдена: {}", id);
                    return new ResponseEntity<>(mapToDTO(bookStyles), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    // LOGGING ADDED
                    logger.warn("Связь книги со стилем не найдена: {}", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // Получение всех связей книг со стилями
    @GetMapping
    public ResponseEntity<List<BookStylesDTO>> getAllBookStyles() {
        // LOGGING ADDED
        logger.info("Получен запрос на получение всех связей книги со стилями");

        List<BookStylesDTO> bookStyles = bookStylesService.getAllBookStyles()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("Возвращено {} связей книги со стилями", bookStyles.size());

        return new ResponseEntity<>(bookStyles, HttpStatus.OK);
    }

    // Обновление связи книги со стилем
    @PutMapping("/{bookIsbn}/{styleId}")
    public ResponseEntity<BookStylesDTO> updateBookStyles(@PathVariable String bookIsbn, @PathVariable Long styleId, @Valid @RequestBody BookStylesDTO bookStylesDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на обновление связи книги со стилем: bookIsbn={}, styleId={}. Новые данные: {}",
                bookIsbn, styleId, bookStylesDTO);

        try {
            BookStylesId oldId = new BookStylesId(bookIsbn, styleId);
            BookStyles bookStyles = new BookStyles();
            bookStyles.setId(new BookStylesId(bookStylesDTO.getBookIsbn(), bookStylesDTO.getStyleId()));

            // Проверка существования новой книги и нового стиля
            bookStyles.setBook(bookService.getBookByIsbn(bookStylesDTO.getBookIsbn())
                    .orElseThrow(() -> new RuntimeException("Книга не найдена: " + bookStylesDTO.getBookIsbn())));
            bookStyles.setStyleEntity(stylesService.getStyleById(bookStylesDTO.getStyleId())
                    .orElseThrow(() -> new RuntimeException("Стиль не найден: " + bookStylesDTO.getStyleId())));

            // Обновление связи
            BookStyles updatedBookStyles = bookStylesService.updateBookStyles(oldId, bookStyles);

            // LOGGING ADDED
            logger.info("Связь книги со стилем успешно обновлена: старый ключ={}, новый ключ={}",
                    oldId, updatedBookStyles.getId());

            BookStylesDTO responseDTO = mapToDTO(updatedBookStyles);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при обновлении связи книги со стилем: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление связи книги со стилем
    @DeleteMapping("/{bookIsbn}/{styleId}")
    public ResponseEntity<Void> deleteBookStyles(@PathVariable String bookIsbn, @PathVariable Long styleId) {
        // LOGGING ADDED
        logger.info("Получен запрос на удаление связи книги со стилем: bookIsbn={}, styleId={}", bookIsbn, styleId);

        try {
            BookStylesId id = new BookStylesId(bookIsbn, styleId);
            bookStylesService.deleteBookStyles(id);

            // LOGGING ADDED
            logger.info("Связь книги со стилем удалена: {}", id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при удалении связи книги со стилем: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Ручной маппинг сущности в DTO
    private BookStylesDTO mapToDTO(BookStyles bookStyles) {
        BookStylesDTO dto = new BookStylesDTO();
        dto.setBookIsbn(bookStyles.getBook().getIsbn());
        dto.setStyleId(bookStyles.getStyleEntity().getId());
        return dto;
    }

    // Ручной маппинг DTO в сущность
    private BookStyles mapToEntity(BookStylesDTO dto) {
        BookStyles bookStyles = new BookStyles();
        BookStylesId id = new BookStylesId(dto.getBookIsbn(), dto.getStyleId());
        bookStyles.setId(id);
        return bookStyles;
    }
}
