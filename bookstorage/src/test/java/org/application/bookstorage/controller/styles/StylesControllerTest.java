package org.application.bookstorage.controller.styles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.bookstorage.dao.Styles;
import org.application.bookstorage.dto.StylesDTO;
import org.application.bookstorage.service.styles.StylesService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StylesController.class)
class StylesControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(StylesControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StylesService stylesService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createStyle_ShouldReturnCreated() throws Exception {
        // Arrange
        logger.info("Тест контроллера: createStyle_ShouldReturnCreated");
        StylesDTO dto = new StylesDTO();
        dto.setName("Новый Стиль");

        Styles created = new Styles(1L, "Новый Стиль", null);
        when(stylesService.createStyle(any(Styles.class))).thenReturn(created);

        // Act & Assert
        mockMvc.perform(post("/api/styles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Новый Стиль"));
    }

    @Test
    void createStyle_ShouldReturnBadRequestIfError() throws Exception {
        // Arrange
        logger.info("Тест контроллера: createStyle_ShouldReturnBadRequestIfError");
        when(stylesService.createStyle(any(Styles.class))).thenThrow(new RuntimeException("Error"));

        StylesDTO dto = new StylesDTO();
        dto.setName("BadStyle");

        // Act & Assert
        mockMvc.perform(post("/api/styles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStyleById_ShouldReturnOkIfFound() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getStyleById_ShouldReturnOkIfFound");
        Styles style = new Styles(1L, "Стиль", null);

        when(stylesService.getStyleById(1L)).thenReturn(Optional.of(style));

        // Act & Assert
        mockMvc.perform(get("/api/styles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Стиль"));
    }

    @Test
    void getStyleById_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getStyleById_ShouldReturnNotFoundIfMissing");
        when(stylesService.getStyleById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/styles/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStyles_ShouldReturnOk() throws Exception {
        // Arrange
        logger.info("Тест контроллера: getAllStyles_ShouldReturnOk");
        when(stylesService.getAllStyles()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/styles"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStyle_ShouldReturnOkIfFound() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateStyle_ShouldReturnOkIfFound");
        StylesDTO dto = new StylesDTO();
        dto.setName("Обновленный стиль");

        Styles updated = new Styles(1L, "Обновленный стиль", null);
        when(stylesService.updateStyle(eq(1L), any(Styles.class))).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/api/styles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленный стиль"));
    }

    @Test
    void updateStyle_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: updateStyle_ShouldReturnNotFoundIfMissing");
        when(stylesService.updateStyle(eq(999L), any(Styles.class))).thenThrow(new RuntimeException("Not found"));

        StylesDTO dto = new StylesDTO();
        dto.setName("Неизвестный");

        // Act & Assert
        mockMvc.perform(put("/api/styles/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStyle_ShouldReturnNoContentIfDeleted() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteStyle_ShouldReturnNoContentIfDeleted");

        // Act & Assert
        mockMvc.perform(delete("/api/styles/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(stylesService, times(1)).deleteStyle(1L);
    }

    @Test
    void deleteStyle_ShouldReturnNotFoundIfMissing() throws Exception {
        // Arrange
        logger.info("Тест контроллера: deleteStyle_ShouldReturnNotFoundIfMissing");
        doThrow(new RuntimeException("Not found")).when(stylesService).deleteStyle(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/styles/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchStyles_ShouldReturnOk() throws Exception {
        // Arrange
        logger.info("Тест контроллера: searchStyles_ShouldReturnOk");
        Styles style = new Styles(1L, "Ужасы", null);
        when(stylesService.searchStylesByName("уж")).thenReturn(Collections.singletonList(style));

        // Act & Assert
        mockMvc.perform(get("/api/styles/search").param("q", "уж"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ужасы"));
    }
}
