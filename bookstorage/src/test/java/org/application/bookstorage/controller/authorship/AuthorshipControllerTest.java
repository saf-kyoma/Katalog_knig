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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createAuthorship_ShouldReturnCreated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: createAuthorship_ShouldReturnCreated");

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

        // Act & Assert
        mockMvc.perform(post("/api/authorships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAuthorshipById_ShouldReturnOkIfFound() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAuthorshipById_ShouldReturnOkIfFound");

        Authorship authorship = new Authorship(
                new AuthorshipId("ISBN-123", 1),
                new Book(),
                new Author()
        );
        when(authorshipService.getAuthorshipById(new AuthorshipId("ISBN-123", 1)))
                .thenReturn(Optional.of(authorship));

        // Act & Assert
        mockMvc.perform(get("/api/authorships/ISBN-123/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuthorshipById_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAuthorshipById_ShouldReturnNotFoundIfMissing");

        when(authorshipService.getAuthorshipById(new AuthorshipId("ISBN-999", 999)))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/authorships/ISBN-999/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAuthorships_ShouldReturnOk() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAllAuthorships_ShouldReturnOk");
        when(authorshipService.getAllAuthorships()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/authorships"))
                .andExpect(status().isOk());
    }

    @Test
    void updateAuthorship_ShouldReturnOkIfUpdated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateAuthorship_ShouldReturnOkIfUpdated");
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

        // Существующий объект (предположим, нужен для логики)
        Book existingBook = new Book();
        existingBook.setIsbn("ISBN-123");
        Author existingAuthor = new Author();
        existingAuthor.setId(1);
        Authorship old = new Authorship(oldId, existingBook, existingAuthor);

        when(authorshipService.updateAuthorship(any(AuthorshipId.class), any(Authorship.class))).thenReturn(old);

        // Act & Assert
        mockMvc.perform(put("/api/authorships/ISBN-123/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAuthorship_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateAuthorship_ShouldReturnNotFoundIfMissing");
        AuthorshipDTO dto = new AuthorshipDTO();
        dto.setBookIsbn("NoBook");
        dto.setAuthorId(999);

        when(bookService.getBookByIsbn("NoBook")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/authorships/ISBN-123/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthorship_ShouldReturnNoContent() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteAuthorship_ShouldReturnNoContent");

        // Act & Assert
        mockMvc.perform(delete("/api/authorships/{bookIsbn}/{authorId}", "ISBN-123", 1))
                .andExpect(status().isNoContent());

        verify(authorshipService, times(1))
                .deleteAuthorship(new AuthorshipId("ISBN-123", 1));
    }

    @Test
    void deleteAuthorship_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteAuthorship_ShouldReturnNotFoundIfMissing");

        doThrow(new RuntimeException("Not found")).when(authorshipService)
                .deleteAuthorship(new AuthorshipId("NOBOOK", 999));

        // Act & Assert
        mockMvc.perform(delete("/api/authorships/NOBOOK/999"))
                .andExpect(status().isNotFound());
    }
}
