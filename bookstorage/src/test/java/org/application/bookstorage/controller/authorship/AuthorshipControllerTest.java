package org.application.bookstorage.controller.authorship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;
import org.application.bookstorage.dao.Author;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dto.AuthorshipDTO;
import org.application.bookstorage.service.authorship.AuthorshipService;
import org.application.bookstorage.service.author.AuthorService;
import org.application.bookstorage.service.book.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;


import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthorshipController.class)
class AuthorshipControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorshipControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorshipService authorshipService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @Test
    void createAuthorship_ShouldReturnCreated() throws Exception {
        logger.info("Тест контроллера: createAuthorship_ShouldReturnCreated");
        ObjectMapper objectMapper = new ObjectMapper();

        AuthorshipDTO dto = new AuthorshipDTO();
        dto.setBookIsbn("ISBN-123");
        dto.setAuthorId(1);

        Book book = new Book();
        book.setIsbn("ISBN-123");

        Author author = new Author();
        author.setId(1);

        when(bookService.getBookByIsbn("ISBN-123")).thenReturn(Optional.of(book));
        when(authorService.getAuthorById(1)).thenReturn(Optional.of(author));
        when(authorshipService.createAuthorship(any(Authorship.class)))
                .thenReturn(new Authorship(new AuthorshipId("ISBN-123", 1), book, author));

        mockMvc.perform(post("/api/authorships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAuthorshipById_ShouldReturnOkIfFound() throws Exception {
        logger.info("Тест контроллера: getAuthorshipById_ShouldReturnOkIfFound");
        Authorship authorship = new Authorship(
                new AuthorshipId("ISBN-123", 1),
                new Book(),
                new Author()
        );

        when(authorshipService.getAuthorshipById(new AuthorshipId("ISBN-123", 1)))
                .thenReturn(Optional.of(authorship));

        mockMvc.perform(get("/api/authorships/ISBN-123/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuthorshipById_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: getAuthorshipById_ShouldReturnNotFoundIfMissing");
        when(authorshipService.getAuthorshipById(new AuthorshipId("ISBN-999", 999)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/authorships/ISBN-999/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAuthorships_ShouldReturnOk() throws Exception {
        logger.info("Тест контроллера: getAllAuthorships_ShouldReturnOk");
        when(authorshipService.getAllAuthorships()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/authorships"))
                .andExpect(status().isOk());
    }

    @Test
    void updateAuthorship_ShouldReturnOkIfUpdated() throws Exception {
        logger.info("Тест контроллера: updateAuthorship_ShouldReturnOkIfUpdated");
        ObjectMapper objectMapper = new ObjectMapper();

        AuthorshipDTO dto = new AuthorshipDTO();
        dto.setBookIsbn("ISBN-456");
        dto.setAuthorId(2);

        // Мокаем получение новой книги и нового автора
        Book newBook = new Book();
        newBook.setIsbn("ISBN-456");
        Author newAuthor = new Author();
        newAuthor.setId(2);

        when(bookService.getBookByIsbn("ISBN-456")).thenReturn(Optional.of(newBook));
        when(authorService.getAuthorById(2)).thenReturn(Optional.of(newAuthor));

        // Старый ID, полученный из URL: (ISBN-123, 1)
        AuthorshipId oldId = new AuthorshipId("ISBN-123", 1);

        // Формируем корректный объект Authorship (без null для book и author)
        Book existingBook = new Book();
        existingBook.setIsbn("ISBN-123");
        Author existingAuthor = new Author();
        existingAuthor.setId(1);
        Authorship old = new Authorship(oldId, existingBook, existingAuthor);

        // Мокаем вызов updateAuthorship, чтобы вернуть 'old'
        when(authorshipService.updateAuthorship(any(AuthorshipId.class), any(Authorship.class))).thenReturn(old);

        mockMvc.perform(put("/api/authorships/ISBN-123/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }



    @Test
    void updateAuthorship_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: updateAuthorship_ShouldReturnNotFoundIfMissing");
        ObjectMapper objectMapper = new ObjectMapper();

        AuthorshipDTO dto = new AuthorshipDTO();
        dto.setBookIsbn("NoBook");
        dto.setAuthorId(999);

        when(bookService.getBookByIsbn("NoBook")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/authorships/ISBN-123/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthorship_ShouldReturnNoContent() throws Exception {
        logger.info("Тест контроллера: deleteAuthorship_ShouldReturnNoContent");

        mockMvc.perform(delete("/api/authorships/{bookIsbn}/{authorId}", "ISBN-123", 1))
                .andExpect(status().isNoContent());
        verify(authorshipService, times(1))
                .deleteAuthorship(new AuthorshipId("ISBN-123", 1));
    }

    @Test
    void deleteAuthorship_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: deleteAuthorship_ShouldReturnNotFoundIfMissing");

        doThrow(new RuntimeException("Not found")).when(authorshipService)
                .deleteAuthorship(new AuthorshipId("NOBOOK", 999));

        mockMvc.perform(delete("/api/authorships/NOBOOK/999"))
                .andExpect(status().isNotFound());
    }
}

