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

@RestController
@RequestMapping("/api/book-styles")
@RequiredArgsConstructor
public class BookStylesController {

    private final BookStylesService bookStylesService;
    private final BookService bookService;
    private final StylesService stylesService;

    // Создание связи книги со стилем
    @PostMapping
    public ResponseEntity<BookStylesDTO> createBookStyles(@Valid @RequestBody BookStylesDTO bookStylesDTO) {
        try {
            // Проверка существования книги и стиля
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
            BookStylesDTO responseDTO = mapToDTO(createdBookStyles);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение связи книги со стилем по ключу
    @GetMapping("/{bookIsbn}/{styleId}")
    public ResponseEntity<BookStylesDTO> getBookStylesById(@PathVariable String bookIsbn, @PathVariable Long styleId) {
        BookStylesId id = new BookStylesId(bookIsbn, styleId);
        return bookStylesService.getBookStylesById(id)
                .map(bookStyles -> new ResponseEntity<>(mapToDTO(bookStyles), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Получение всех связей книг со стилями
    @GetMapping
    public ResponseEntity<List<BookStylesDTO>> getAllBookStyles() {
        List<BookStylesDTO> bookStyles = bookStylesService.getAllBookStyles()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(bookStyles, HttpStatus.OK);
    }

    // Обновление связи книги со стилем
    @PutMapping("/{bookIsbn}/{styleId}")
    public ResponseEntity<BookStylesDTO> updateBookStyles(@PathVariable String bookIsbn, @PathVariable Long styleId, @Valid @RequestBody BookStylesDTO bookStylesDTO) {
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
            BookStylesDTO responseDTO = mapToDTO(updatedBookStyles);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление связи книги со стилем
    @DeleteMapping("/{bookIsbn}/{styleId}")
    public ResponseEntity<Void> deleteBookStyles(@PathVariable String bookIsbn, @PathVariable Long styleId) {
        try {
            BookStylesId id = new BookStylesId(bookIsbn, styleId);
            bookStylesService.deleteBookStyles(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
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
        // Связи устанавливаются в методах создания и обновления
        return bookStyles;
    }
}
