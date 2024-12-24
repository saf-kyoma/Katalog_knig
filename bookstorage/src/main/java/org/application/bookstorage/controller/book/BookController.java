package org.application.bookstorage.controller.book;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dto.BookDTO;
import org.application.bookstorage.dto.AuthorDTO;
import org.application.bookstorage.service.book.BookService;
import org.application.bookstorage.service.publishingcompany.PublishingCompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final PublishingCompanyService publishingCompanyService;

    // Создание книги
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody BookDTO bookDTO) {
        try {
            // Получение издательства по имени
            Book book = mapToEntity(bookDTO);
            book.setPublishingCompany(publishingCompanyService.getPublishingCompanyByName(bookDTO.getPublishingCompany())
                    .orElseThrow(() -> new RuntimeException("Издательство не найдено: " + bookDTO.getPublishingCompany())));
            Book createdBook = bookService.createBook(book);
            BookDTO responseDTO = mapToDTO(createdBook);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // Обновление книги
    @PutMapping("/{isbn}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable String isbn, @Valid @RequestBody BookDTO bookDTO) {
        try {
            Book bookDetails = mapToEntity(bookDTO);
            // Обновление издательства
            bookDetails.setPublishingCompany(publishingCompanyService.getPublishingCompanyByName(bookDTO.getPublishingCompany())
                    .orElseThrow(() -> new RuntimeException("Издательство не найдено: " + bookDTO.getPublishingCompany())));
            Book updatedBook = bookService.updateBook(isbn, bookDetails);
            BookDTO responseDTO = mapToDTO(updatedBook);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
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
        // Издательство устанавливается в методе создания или обновления
        book.setPageCount(dto.getPageCount());
        book.setLanguage(dto.getLanguage());
        book.setCost(dto.getCost());
        book.setCountOfBooks(dto.getCountOfBooks());
        return book;
    }

    // Ручной маппинг сущности в DTO
    private BookDTO mapToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setIsbn(book.getIsbn());
        dto.setName(book.getName());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setAgeLimit(book.getAgeLimit());
        dto.setPublishingCompany(book.getPublishingCompany() != null ? book.getPublishingCompany().getName() : null);
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

        return dto;
    }
}
