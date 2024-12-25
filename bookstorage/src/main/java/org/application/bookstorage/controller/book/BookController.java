package org.application.bookstorage.controller.book;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Author;
import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.application.bookstorage.dao.Styles;
import org.application.bookstorage.dao.PublishingCompany;
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

    // Создание книги
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
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

            // Преобразование в DTO
            BookDTO responseDTO = mapToDTO(createdBook);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Логирование ошибки (рекомендуется использовать логгер)
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
            return publishingCompanyService.createPublishingCompany(newCompany);
        });
    }

    // Получение книги по ISBN
    @GetMapping("/{isbn}")
    public ResponseEntity<BookDTO> getBookByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn)
                .map(book -> new ResponseEntity<>(mapToDTO(book), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Получение всех книг
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "sort_column") String sortColumn,
            @RequestParam(required = false, name = "sort_order") String sortOrder) {
        try {
            List<Book> books = bookService.getAllBooks(search, sortColumn, sortOrder);
            List<BookDTO> bookDTOs = books.stream().map(this::mapToDTO).collect(Collectors.toList());
            return new ResponseEntity<>(bookDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Логирование ошибки
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Обновление книги
    @PutMapping("/{isbn}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable String isbn, @Valid @RequestBody BookDTO bookDTO) {
        try {
            // Получение или создание издательства
            PublishingCompany publishingCompany = getOrCreatePublishingCompany(bookDTO.getPublishingCompany());

            // Преобразование DTO в сущность
            Book bookDetails = mapToEntity(bookDTO);
            bookDetails.setPublishingCompany(publishingCompany);

            // Обновление книги
            Book updatedBook = bookService.updateBook(isbn, bookDetails);
            BookDTO responseDTO = mapToDTO(updatedBook);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Логирование ошибки
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление книги
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        try {
            bookService.deleteBook(isbn);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
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
        // Издательство устанавливается отдельно
        book.setPageCount(dto.getPageCount());
        book.setLanguage(dto.getLanguage());
        book.setCost(dto.getCost());
        book.setCountOfBooks(dto.getCountOfBooks());
        // Инициализация множеств, если необходимо
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

        // Устанавливаем название издательства
        dto.setPublishingCompany(book.getPublishingCompany().getName());

        dto.setPageCount(book.getPageCount());
        dto.setLanguage(book.getLanguage());
        dto.setCost(book.getCost());
        dto.setCountOfBooks(book.getCountOfBooks());

        // Маппинг авторов
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

        // Маппинг жанров
        List<String> genres = book.getBookStyles().stream()
                .map(bookStyle -> bookStyle.getStyleEntity().getName())
                .collect(Collectors.toList());

        dto.setGenres(genres);

        return dto;
    }
}
