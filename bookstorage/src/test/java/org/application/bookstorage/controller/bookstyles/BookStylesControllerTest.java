package org.application.bookstorage.controller.bookstyles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dao.Styles;
import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.application.bookstorage.dto.BookStylesDTO;
import org.application.bookstorage.service.book.BookService;
import org.application.bookstorage.service.bookstyles.BookStylesService;
import org.application.bookstorage.service.styles.StylesService;
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

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookStylesController.class)
class BookStylesControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(BookStylesControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookStylesService bookStylesService;
    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private StylesService stylesService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createBookStyles_ShouldReturnCreated() throws Exception {
        logger.info("Тест контроллера: createBookStyles_ShouldReturnCreated");

        BookStylesDTO dto = new BookStylesDTO();
        dto.setBookIsbn("ISBN-123");
        dto.setStyleId(1L);

        Book book = new Book();
        book.setIsbn("ISBN-123");
        Styles style = new Styles();
        style.setId(1L);

        when(bookService.getBookByIsbn("ISBN-123")).thenReturn(Optional.of(book));
        when(stylesService.getStyleById(1L)).thenReturn(Optional.of(style));

        BookStyles entity = new BookStyles(new BookStylesId("ISBN-123", 1L), book, style);
        when(bookStylesService.createBookStyles(any(BookStyles.class))).thenReturn(entity);

        mockMvc.perform(post("/api/book-styles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getBookStylesById_ShouldReturnOkIfFound() throws Exception {
        logger.info("Тест контроллера: getBookStylesById_ShouldReturnOkIfFound");

        when(bookStylesService.getBookStylesById(new BookStylesId("ISBN-123", 1L)))
                .thenReturn(Optional.of(new BookStyles()));

        mockMvc.perform(get("/api/book-styles/ISBN-123/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookStylesById_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: getBookStylesById_ShouldReturnNotFoundIfMissing");

        when(bookStylesService.getBookStylesById(new BookStylesId("NOBOOK", 999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/book-styles/NOBOOK/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBookStyles_ShouldReturnOk() throws Exception {
        logger.info("Тест контроллера: getAllBookStyles_ShouldReturnOk");

        when(bookStylesService.getAllBookStyles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/book-styles"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookStyles_ShouldReturnOkIfUpdated() throws Exception {
        logger.info("Тест контроллера: updateBookStyles_ShouldReturnOkIfUpdated");

        BookStylesDTO dto = new BookStylesDTO();
        dto.setBookIsbn("ISBN-456");
        dto.setStyleId(2L);

        Book book = new Book();
        book.setIsbn("ISBN-456");
        Styles style = new Styles();
        style.setId(2L);

        when(bookService.getBookByIsbn("ISBN-456")).thenReturn(Optional.of(book));
        when(stylesService.getStyleById(2L)).thenReturn(Optional.of(style));

        BookStyles oldEntity = new BookStyles(new BookStylesId("ISBN-123", 1L), null, null);
        BookStylesId oldId = new BookStylesId("ISBN-123", 1L);

        when(bookStylesService.updateBookStyles(eq(oldId), any(BookStyles.class))).thenReturn(oldEntity);
        mockMvc.perform(put("/api/book-styles/ISBN-123/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookStyles_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: updateBookStyles_ShouldReturnNotFoundIfMissing");

        BookStylesDTO dto = new BookStylesDTO();
        dto.setBookIsbn("NoBook");
        dto.setStyleId(999L);

        when(bookService.getBookByIsbn("NoBook")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/book-styles/ISBN-123/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBookStyles_ShouldReturnNoContent() throws Exception {
        logger.info("Тест контроллера: deleteBookStyles_ShouldReturnNoContent");

        mockMvc.perform(delete("/api/book-styles/{bookIsbn}/{styleId}", "ISBN-123", 1L))
                .andExpect(status().isNoContent());

        verify(bookStylesService, times(1))
                .deleteBookStyles(new BookStylesId("ISBN-123", 1L));
    }

    @Test
    void deleteBookStyles_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: deleteBookStyles_ShouldReturnNotFoundIfMissing");

        doThrow(new RuntimeException("Not found")).when(bookStylesService)
                .deleteBookStyles(new BookStylesId("NOBOOK", 999L));

        mockMvc.perform(delete("/api/book-styles/NOBOOK/999"))
                .andExpect(status().isNotFound());
    }
}

