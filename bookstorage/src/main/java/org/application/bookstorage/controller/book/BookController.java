package org.application.bookstorage.controller.book;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.*;
import org.application.bookstorage.dto.BookDTO;
import org.application.bookstorage.dto.AuthorDTO;
import org.application.bookstorage.service.author.AuthorService;
import org.application.bookstorage.service.authorship.AuthorshipService;
import org.application.bookstorage.service.book.BookService;
import org.application.bookstorage.service.bookstyles.BookStylesService;
import org.application.bookstorage.service.publishingcompany.PublishingCompanyService;
import org.application.bookstorage.service.styles.StylesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final PublishingCompanyService publishingCompanyService;
    private final AuthorService authorService;
    private final AuthorshipService authorshipService;
    private final StylesService stylesService;
    private final BookStylesService bookStylesService;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    /**
     * Эндпоинт для массового удаления книг.
     * Метод: DELETE
     * URL: /api/books/bulk-delete
     * Тело запроса: список ISBN книг для удаления
     */
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deleteBooks(@RequestBody List<String> isbns) {
        // LOGGING ADDED
        logger.info("Получен запрос на массовое удаление книг: {}", isbns);

        try {
            bookService.deleteBooks(isbns);

            // LOGGING ADDED
            logger.info("Массовое удаление книг успешно завершено.");

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при массовом удалении книг: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Создание книги
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на создание книги: {}", bookDTO);

        try {
            // Получение или создание издательства
            PublishingCompany publishingCompany = getOrCreatePublishingCompany(bookDTO.getPublishingCompany());

            // Создание книги
            Book book = new Book();
            book.setIsbn(bookDTO.getIsbn());
            book.setName(bookDTO.getName());
            book.setPublicationYear(bookDTO.getPublicationYear());
            book.setAgeLimit(bookDTO.getAgeLimit());
            book.setPublishingCompany(publishingCompany);
            book.setPageCount(bookDTO.getPageCount());
            book.setLanguage(bookDTO.getLanguage());
            book.setCost(bookDTO.getCost());
            book.setCountOfBooks(bookDTO.getCountOfBooks());

            // Инициализация множеств, если необходимо
            if (book.getAuthorships() == null) {
                book.setAuthorships(new java.util.HashSet<>());
            }
            if (book.getBookStyles() == null) {
                book.setBookStyles(new java.util.HashSet<>());
            }

            // Обработка авторов
            List<Authorship> authorships = bookDTO.getAuthors().stream().map(authorDTO -> {
                Author author;
                if (authorDTO.getId() != null) { // Проверка, предоставлен ли ID
                    author = authorService.getAuthorById(authorDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Автор не найден с ID: " + authorDTO.getId()));
                } else { // Создание нового автора
                    author = new Author();
                    author.setFio(authorDTO.getFio());
                    author.setBirthDate(authorDTO.getBirthDate());
                    author.setCountry(authorDTO.getCountry());
                    author.setNickname(authorDTO.getNickname());
                    author = authorService.createAuthor(author);
                }
                Authorship authorship = new Authorship();
                authorship.setBook(book);
                authorship.setAuthor(author);
                authorship.setId(new org.application.bookstorage.dao.AuthorshipId(book.getIsbn(), author.getId()));
                return authorship;
            }).collect(Collectors.toList());

            book.getAuthorships().addAll(authorships);

            // Обработка жанров
            List<String> genres = bookDTO.getGenres();
            for (String genreName : genres) {
                Styles style = stylesService.getStyleByName(genreName)
                        .orElseGet(() -> stylesService.createStyle(new Styles(null, genreName, null)));
                BookStyles bookStyle = new BookStyles();
                bookStyle.setBook(book);
                bookStyle.setStyleEntity(style);
                bookStyle.setId(new BookStylesId(book.getIsbn(), style.getId()));
                book.getBookStyles().add(bookStyle);
            }

            // Сохранение книги
            Book createdBook = bookService.createBook(book);

            // LOGGING ADDED
            logger.info("Книга успешно создана: ISBN={}", createdBook.getIsbn());

            // Преобразование в DTO
            BookDTO responseDTO = mapToDTO(createdBook);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при создании книги: {}", e.getMessage(), e);
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Метод для получения или создания издательства
    private PublishingCompany getOrCreatePublishingCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new RuntimeException("Название издательства не может быть пустым");
        }
        Optional<PublishingCompany> optionalCompany = publishingCompanyService.getPublishingCompanyByName(companyName.trim());
        return optionalCompany.orElseGet(() -> {
            PublishingCompany newCompany = new PublishingCompany();
            newCompany.setName(companyName.trim());
            // Устанавливаем другие поля издательства, если необходимо

            // LOGGING ADDED
            logger.info("Издательство не найдено, создаём новое: {}", newCompany.getName());

            return publishingCompanyService.createPublishingCompany(newCompany);
        });
    }

    // Получение книги по ISBN
    @GetMapping("/{isbn}")
    public ResponseEntity<BookDTO> getBookByIsbn(@PathVariable String isbn) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение книги по ISBN: {}", isbn);

        return bookService.getBookByIsbn(isbn)
                .map(book -> {
                    // LOGGING ADDED
                    logger.info("Книга найдена: ISBN={}", book.getIsbn());
                    return new ResponseEntity<>(mapToDTO(book), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    // LOGGING ADDED
                    logger.warn("Книга с ISBN {} не найдена", isbn);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // Получение всех книг
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "sort_column") String sortColumn,
            @RequestParam(required = false, name = "sort_order") String sortOrder) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение всех книг. search={}, sort_column={}, sort_order={}",
                search, sortColumn, sortOrder);

        try {
            List<Book> books = bookService.getAllBooks(search, sortColumn, sortOrder);
            List<BookDTO> bookDTOs = books.stream().map(this::mapToDTO).collect(Collectors.toList());

            // LOGGING ADDED
            logger.info("Поиск завершён. Найдено {} книг.", bookDTOs.size());

            return new ResponseEntity<>(bookDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при получении списка книг: {}", e.getMessage(), e);
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Обновление книги
    @PutMapping("/{isbn}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable String isbn, @Valid @RequestBody BookDTO bookDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на обновление книги ISBN={}. Новые данные: {}", isbn, bookDTO);

        try {
            // Получение или создание издательства
            PublishingCompany publishingCompany = getOrCreatePublishingCompany(bookDTO.getPublishingCompany());

            // Получаем существующую книгу
            Book existingBook = bookService.getBookByIsbn(isbn)
                    .orElseThrow(() -> new RuntimeException("Книга не найдена с ISBN " + isbn));

            // Обновляем основные поля
            existingBook.setName(bookDTO.getName());
            existingBook.setPublicationYear(bookDTO.getPublicationYear());
            existingBook.setAgeLimit(bookDTO.getAgeLimit());
            existingBook.setPublishingCompany(publishingCompany);
            existingBook.setPageCount(bookDTO.getPageCount());
            existingBook.setLanguage(bookDTO.getLanguage());
            existingBook.setCost(bookDTO.getCost());
            existingBook.setCountOfBooks(bookDTO.getCountOfBooks());

            // 1. Обновляем авторов:
            existingBook.getAuthorships().clear();
            List<Authorship> newAuthorships = bookDTO.getAuthors().stream().map(authorDTO -> {
                Author author;
                if (authorDTO.getId() != null) {
                    author = authorService.getAuthorById(authorDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Автор не найден с ID: " + authorDTO.getId()));
                } else {
                    author = new Author();
                    author.setFio(authorDTO.getFio());
                    author.setBirthDate(authorDTO.getBirthDate());
                    author.setCountry(authorDTO.getCountry());
                    author.setNickname(authorDTO.getNickname());
                    author = authorService.createAuthor(author);
                }
                Authorship authorship = new Authorship();
                authorship.setId(new AuthorshipId(existingBook.getIsbn(), author.getId()));
                authorship.setBook(existingBook);
                authorship.setAuthor(author);
                return authorship;
            }).collect(Collectors.toList());
            existingBook.getAuthorships().addAll(newAuthorships);

            // 2. Обновляем жанры (BookStyles):
            existingBook.getBookStyles().clear();
            List<String> genres = bookDTO.getGenres();
            for (String genreName : genres) {
                Styles style = stylesService.getStyleByName(genreName)
                        .orElseGet(() -> stylesService.createStyle(new Styles(null, genreName, null)));
                BookStyles bookStyle = new BookStyles();
                bookStyle.setId(new BookStylesId(existingBook.getIsbn(), style.getId()));
                bookStyle.setBook(existingBook);
                bookStyle.setStyleEntity(style);
                existingBook.getBookStyles().add(bookStyle);
            }

            // Сохраняем обновлённую книгу
            Book updatedBook = bookService.updateBook(isbn, existingBook);

            // LOGGING ADDED
            logger.info("Книга с ISBN {} успешно обновлена", isbn);

            BookDTO responseDTO = mapToDTO(updatedBook);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при обновлении книги ISBN {}: {}", isbn, e.getMessage(), e);
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление книги
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        // LOGGING ADDED
        logger.info("Получен запрос на удаление книги ISBN={}", isbn);

        try {
            bookService.deleteBook(isbn);

            // LOGGING ADDED
            logger.info("Книга с ISBN {} успешно удалена", isbn);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при удалении книги ISBN {}: {}", isbn, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Ручной маппинг DTO в сущность
    private Book mapToEntity(BookDTO dto) {
        Book book = new Book();
        book.setIsbn(dto.getIsbn());
        book.setName(dto.getName());
        book.setPublicationYear(dto.getPublicationYear());
        book.setAgeLimit(dto.getAgeLimit());
        book.setPageCount(dto.getPageCount());
        book.setLanguage(dto.getLanguage());
        book.setCost(dto.getCost());
        book.setCountOfBooks(dto.getCountOfBooks());
        if (book.getAuthorships() == null) {
            book.setAuthorships(new java.util.HashSet<>());
        }
        if (book.getBookStyles() == null) {
            book.setBookStyles(new java.util.HashSet<>());
        }
        return book;
    }

    // Ручной маппинг сущности в DTO
    private BookDTO mapToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setIsbn(book.getIsbn());
        dto.setName(book.getName());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setAgeLimit(book.getAgeLimit());
        dto.setPublishingCompany(book.getPublishingCompany().getName());
        dto.setPageCount(book.getPageCount());
        dto.setLanguage(book.getLanguage());
        dto.setCost(book.getCost());
        dto.setCountOfBooks(book.getCountOfBooks());

        List<AuthorDTO> authors = book.getAuthorships().stream()
                .map(authorship -> {
                    AuthorDTO authorDTO = new AuthorDTO();
                    authorDTO.setId(authorship.getAuthor().getId());
                    authorDTO.setFio(authorship.getAuthor().getFio());
                    authorDTO.setBirthDate(authorship.getAuthor().getBirthDate());
                    authorDTO.setCountry(authorship.getAuthor().getCountry());
                    authorDTO.setNickname(authorship.getAuthor().getNickname());
                    return authorDTO;
                }).collect(Collectors.toList());

        dto.setAuthors(authors);

        List<String> genres = book.getBookStyles().stream()
                .map(bookStyle -> bookStyle.getStyleEntity().getName())
                .collect(Collectors.toList());

        dto.setGenres(genres);

        return dto;
    }
}
