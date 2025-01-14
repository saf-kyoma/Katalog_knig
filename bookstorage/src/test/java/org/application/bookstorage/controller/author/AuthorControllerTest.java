package org.application.bookstorage.controller.author;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.bookstorage.dao.Author;
import org.application.bookstorage.dto.AuthorDTO;
import org.application.bookstorage.service.author.AuthorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

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
@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createAuthor_ShouldReturnCreated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: createAuthor_ShouldReturnCreated");

        AuthorDTO requestDto = new AuthorDTO();
        requestDto.setFio("Новый Автор");
        requestDto.setBirthDate("2000-01-01");
        requestDto.setCountry("Россия");
        requestDto.setNickname("newnick");

        Author created = new Author();
        created.setId(1);
        created.setFio("Новый Автор");

        when(authorService.createAuthor(any(Author.class))).thenReturn(created);

        // Act & Assert
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fio").value("Новый Автор"));
    }

    @Test
    void getAuthorById_ShouldReturnOkIfFound() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAuthorById_ShouldReturnOkIfFound");

        Author author = new Author();
        author.setId(1);
        author.setFio("Автор 1");

        when(authorService.getAuthorById(1)).thenReturn(Optional.of(author));

        // Act & Assert
        mockMvc.perform(get("/api/authors/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("Автор 1"));
    }

    @Test
    void getAuthorById_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAuthorById_ShouldReturnNotFoundIfMissing");

        when(authorService.getAuthorById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/authors/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAuthors_ShouldReturnOkAndList() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAllAuthors_ShouldReturnOkAndList");

        Author author1 = new Author();
        author1.setId(1);
        author1.setFio("Автор 1");

        Author author2 = new Author();
        author2.setId(2);
        author2.setFio("Автор 2");

        when(authorService.getAllAuthors()).thenReturn(Arrays.asList(author1, author2));

        // Act & Assert
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fio").value("Автор 1"))
                .andExpect(jsonPath("$[1].fio").value("Автор 2"));
    }

    @Test
    void updateAuthor_ShouldReturnOkIfUpdated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateAuthor_ShouldReturnOkIfUpdated");

        AuthorDTO requestDto = new AuthorDTO();
        requestDto.setFio("Обновлённый Автор");
        requestDto.setCountry("Россия");

        Author updated = new Author();
        updated.setId(1);
        updated.setFio("Обновлённый Автор");

        when(authorService.updateAuthor(eq(1), any(Author.class))).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/api/authors/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("Обновлённый Автор"));
    }

    @Test
    void updateAuthor_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateAuthor_ShouldReturnNotFoundIfMissing");

        when(authorService.updateAuthor(eq(999), any(Author.class))).thenThrow(new RuntimeException("Not found"));

        AuthorDTO requestDto = new AuthorDTO();
        requestDto.setFio("Кто-то");

        // Act & Assert
        mockMvc.perform(put("/api/authors/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_ShouldReturnNoContentIfDeleted() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteAuthor_ShouldReturnNoContentIfDeleted");

        // Act & Assert
        mockMvc.perform(delete("/api/authors/{id}", 1))
                .andExpect(status().isNoContent());

        verify(authorService, times(1)).deleteAuthor(1);
    }

    @Test
    void deleteAuthor_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteAuthor_ShouldReturnNotFoundIfMissing");

        doThrow(new RuntimeException("Not found")).when(authorService).deleteAuthor(999);

        // Act & Assert
        mockMvc.perform(delete("/api/authors/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthorsBulk_ShouldReturnNoContent() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteAuthorsBulk_ShouldReturnNoContent");

        List<Integer> ids = Arrays.asList(1, 2, 3);

        // Act & Assert
        mockMvc.perform(delete("/api/authors/bulk-delete?removeEverything=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isNoContent());

        verify(authorService, times(1)).deleteAuthors(eq(ids), eq(true));
    }

    @Test
    void searchAuthors_ShouldReturnList() throws Exception {
        // Arrange
        logger.info("Тест контроллера: searchAuthors_ShouldReturnList");

        Author author = new Author();
        author.setId(1);
        author.setFio("Иванов");

        when(authorService.searchAuthors("Иван")).thenReturn(Collections.singletonList(author));

        // Act & Assert
        mockMvc.perform(get("/api/authors/search").param("q", "Иван"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fio").value("Иванов"));
    }
}
