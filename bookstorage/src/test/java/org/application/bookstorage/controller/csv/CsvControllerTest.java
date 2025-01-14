package org.application.bookstorage.controller.csv;

import org.application.bookstorage.service.csv.CsvService;
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

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CsvController.class)
class CsvControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(CsvControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvService csvService;

    @Test
    void exportData_ShouldReturnOk() throws Exception {
        // Arrange
        logger.info("Тест контроллера: exportData_ShouldReturnOk");

        // Act & Assert
        mockMvc.perform(post("/api/csv/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(csvService, times(1)).exportData();
    }

    @Test
    void exportData_ShouldReturnErrorIfException() throws Exception {
        // Arrange
        logger.info("Тест контроллера: exportData_ShouldReturnErrorIfException");
        doThrow(new IOException("IO Error")).when(csvService).exportData();

        // Act & Assert
        mockMvc.perform(post("/api/csv/export"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка при экспорте данных: IO Error"));
    }

    @Test
    void importData_ShouldReturnOk() throws Exception {
        // Arrange
        logger.info("Тест контроллера: importData_ShouldReturnOk");

        // Act & Assert
        mockMvc.perform(post("/api/csv/import"))
                .andExpect(status().isOk());

        verify(csvService, times(1)).importData();
    }

    @Test
    void importData_ShouldReturnErrorIfException() throws Exception {
        // Arrange
        logger.info("Тест контроллера: importData_ShouldReturnErrorIfException");
        doThrow(new IOException("IO Error")).when(csvService).importData();

        // Act & Assert
        mockMvc.perform(post("/api/csv/import"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка при импорте данных: IO Error"));
    }
}
