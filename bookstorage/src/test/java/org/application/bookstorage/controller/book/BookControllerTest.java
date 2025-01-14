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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deleteBooksBulk_ShouldReturnNoContent() throws Exception {
        logger.info("Тест контроллера: deleteBooksBulk_ShouldReturnNoContent");
        List<String> isbns = Arrays.asList("ISBN-123", "ISBN-456");
        mockMvc.perform(delete("/api/books/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(isbns)))
                .andExpect(status().isNoContent());
        verify(bookService, times(1)).deleteBooks(isbns);
    }


    @Test
    void createBook_ShouldReturnBadRequestIfError() throws Exception {
        logger.info("Тест контроллера: createBook_ShouldReturnBadRequestIfError");
        BookDTO dto = new BookDTO();
        when(bookService.createBook(any(Book.class))).thenThrow(new RuntimeException("Some error"));
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getBookByIsbn_ShouldReturnNotFound() throws Exception {
        logger.info("Тест контроллера: getBookByIsbn_ShouldReturnNotFound");
        when(bookService.getBookByIsbn("NOT-EXIST")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/books/{isbn}", "NOT-EXIST"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBooks_ShouldReturnOk() throws Exception {
        logger.info("Тест контроллера: getAllBooks_ShouldReturnOk");
        when(bookService.getAllBooks(null, null, null)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }


    @Test
    void updateBook_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: updateBook_ShouldReturnNotFoundIfMissing");
        when(bookService.updateBook(eq("NOT-EXIST"), any(Book.class)))
                .thenThrow(new RuntimeException("Not found"));
        BookDTO dto = new BookDTO();
        dto.setIsbn("NOT-EXIST");
        dto.setName("Какая-то книга");
        dto.setPublishingCompany("Some Publisher");
        dto.setAuthors(new ArrayList<>());
        dto.setGenres(new ArrayList<>());
        // Мокаем getBookByIsbn чтобы выбросить исключение
        when(bookService.getBookByIsbn("NOT-EXIST")).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/books/{isbn}", "NOT-EXIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_ShouldReturnNoContent() throws Exception {
        logger.info("Тест контроллера: deleteBook_ShouldReturnNoContent");
        mockMvc.perform(delete("/api/books/{isbn}", "ISBN-123"))
                .andExpect(status().isNoContent());
        verify(bookService, times(1)).deleteBook("ISBN-123");
    }

    @Test
    void deleteBook_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: deleteBook_ShouldReturnNotFoundIfMissing");
        doThrow(new RuntimeException("Not found")).when(bookService).deleteBook("NOT-EXIST");
        mockMvc.perform(delete("/api/books/{isbn}", "NOT-EXIST"))
                .andExpect(status().isNotFound());
    }
}
