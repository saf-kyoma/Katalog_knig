package org.application.bookstorage.controller.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.bookstorage.config.JacksonTestConfig;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dto.BookDTO;
import org.application.bookstorage.service.author.AuthorService;
import org.application.bookstorage.service.authorship.AuthorshipService;
import org.application.bookstorage.service.book.BookService;
import org.application.bookstorage.service.bookstyles.BookStylesService;
import org.application.bookstorage.service.publishingcompany.PublishingCompanyService;
import org.application.bookstorage.service.styles.StylesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
@Import(JacksonTestConfig.class)
class BookControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(BookControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private PublishingCompanyService publishingCompanyService;
    @MockitoBean
    private AuthorService authorService;
    @MockitoBean
    private AuthorshipService authorshipService;
    @MockitoBean
    private StylesService stylesService;
    @MockitoBean
    private BookStylesService bookStylesService;

    // Благодаря @Import(JacksonTestConfig.class) LocalDate будет корректно сериализовываться
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deleteBooksBulk_ShouldReturnNoContent() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteBooksBulk_ShouldReturnNoContent");
        List<String> isbns = Arrays.asList("ISBN-123", "ISBN-456");

        // Act & Assert
        mockMvc.perform(delete("/api/books/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(isbns)))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBooks(isbns);
    }

    @Test
    void createBook_ShouldReturnCreated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: createBook_ShouldReturnCreated");
        BookDTO dto = new BookDTO();
        dto.setIsbn("ISBN-123");
        dto.setName("Тестовая книга");
        dto.setPublicationYear(LocalDate.of(2020, 1, 1));
        dto.setAgeLimit(18f);
        dto.setPublishingCompany("TestPub");
        dto.setPageCount(100);
        dto.setLanguage("Russian");
        dto.setCost(BigDecimal.valueOf(500));
        dto.setCountOfBooks(10);
        dto.setAuthors(new ArrayList<>());
        dto.setGenres(new ArrayList<>());

        Book created = new Book();
        created.setIsbn("ISBN-123");
        created.setName("Тестовая книга");

        when(bookService.createBook(any(Book.class))).thenReturn(created);

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("ISBN-123"));
    }

    @Test
    void createBook_ShouldReturnBadRequestIfError() throws Exception {
        // Arrange
        logger.info("Тест контроллера: createBook_ShouldReturnBadRequestIfError");
        BookDTO dto = new BookDTO();
        // Преднамеренно оставляем поля либо пустыми, либо "невалидными" для имитации ошибки
        when(bookService.createBook(any(Book.class))).thenThrow(new RuntimeException("Some error"));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookByIsbn_ShouldReturnOkIfFound() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getBookByIsbn_ShouldReturnOkIfFound");
        Book book = new Book();
        book.setIsbn("ISBN-123");
        book.setName("Книга 1");

        when(bookService.getBookByIsbn("ISBN-123")).thenReturn(Optional.of(book));

        // Act & Assert
        mockMvc.perform(get("/api/books/{isbn}", "ISBN-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Книга 1"));
    }

    @Test
    void getBookByIsbn_ShouldReturnNotFound() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getBookByIsbn_ShouldReturnNotFound");
        when(bookService.getBookByIsbn("NOT-EXIST")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/books/{isbn}", "NOT-EXIST"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBooks_ShouldReturnOk() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAllBooks_ShouldReturnOk");
        when(bookService.getAllBooks(null, null, null)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBook_ShouldReturnOkIfUpdated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateBook_ShouldReturnOkIfUpdated");

        BookDTO dto = new BookDTO();
        dto.setIsbn("ISBN-123"); // чтобы пройти валидацию @NotBlank
        dto.setName("Новое имя");
        dto.setPublishingCompany("OtherPub"); // тоже @NotBlank
        dto.setAuthors(new ArrayList<>());
        dto.setGenres(new ArrayList<>());

        Book updated = new Book();
        updated.setIsbn("ISBN-123");
        updated.setName("Новое имя");

        when(bookService.updateBook(eq("ISBN-123"), any(Book.class))).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/api/books/{isbn}", "ISBN-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void updateBook_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateBook_ShouldReturnNotFoundIfMissing");

        // Мокаем поведение сервисного слоя
        when(bookService.updateBook(eq("NOT-EXIST"), any(Book.class)))
                .thenThrow(new RuntimeException("Not found"));

        // Создаём DTO, заполняя обязательные поля, чтобы валидация прошла успешно
        BookDTO dto = new BookDTO();
        dto.setIsbn("NOT-EXIST"); // для валидации
        dto.setName("Какая-то книга");
        dto.setPublishingCompany("Some Publisher");
        dto.setAuthors(new ArrayList<>());
        dto.setGenres(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(put("/api/books/{isbn}", "NOT-EXIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_ShouldReturnNoContent() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteBook_ShouldReturnNoContent");

        // Act & Assert
        mockMvc.perform(delete("/api/books/{isbn}", "ISBN-123"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook("ISBN-123");
    }

    @Test
    void deleteBook_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteBook_ShouldReturnNotFoundIfMissing");
        doThrow(new RuntimeException("Not found")).when(bookService).deleteBook("NOT-EXIST");

        // Act & Assert
        mockMvc.perform(delete("/api/books/{isbn}", "NOT-EXIST"))
                .andExpect(status().isNotFound());
    }
}
