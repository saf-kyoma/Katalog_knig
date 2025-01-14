package org.application.bookstorage.service.bookstyles;

import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.application.bookstorage.repository.BookStylesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookStylesServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(BookStylesServiceTest.class);

    @Mock
    private BookStylesRepository bookStylesRepository;

    @InjectMocks
    private BookStylesServiceImpl bookStylesService;

    private BookStylesId id;
    private BookStyles bookStyles;

    @BeforeEach
    void setUp() {
        // Arrange
        id = new BookStylesId("ISBN-123", 1L);
        bookStyles = new BookStyles(id, null, null);
    }

    @Test
    void createBookStyles_ShouldSave() {
        // Arrange
        logger.info("Тест: createBookStyles_ShouldSave");
        when(bookStylesRepository.save(bookStyles)).thenReturn(bookStyles);

        // Act
        BookStyles result = bookStylesService.createBookStyles(bookStyles);

        // Assert
        assertNotNull(result);
        verify(bookStylesRepository, times(1)).save(bookStyles);
    }

    @Test
    void getBookStylesById_ShouldReturnOptional() {
        // Arrange
        logger.info("Тест: getBookStylesById_ShouldReturnOptional");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.of(bookStyles));

        // Act
        Optional<BookStyles> result = bookStylesService.getBookStylesById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bookStyles, result.get());
        verify(bookStylesRepository, times(1)).findById(id);
    }

    @Test
    void getAllBookStyles_ShouldReturnList() {
        // Arrange
        logger.info("Тест: getAllBookStyles_ShouldReturnList");
        List<BookStyles> data = Collections.singletonList(bookStyles);
        when(bookStylesRepository.findAll()).thenReturn(data);

        // Act
        List<BookStyles> result = bookStylesService.getAllBookStyles();

        // Assert
        assertEquals(1, result.size());
        assertEquals(bookStyles, result.get(0));
        verify(bookStylesRepository, times(1)).findAll();
    }

    @Test
    void updateBookStyles_ShouldUpdateIfFound() {
        // Arrange
        logger.info("Тест: updateBookStyles_ShouldUpdateIfFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.of(bookStyles));
        when(bookStylesRepository.save(bookStyles)).thenReturn(bookStyles);

        BookStyles newData = new BookStyles(id, null, null);

        // Act
        BookStyles updated = bookStylesService.updateBookStyles(id, newData);

        // Assert
        assertNotNull(updated);
        // Проверяем, что поля обновились (если бы были какие-то поля)
        verify(bookStylesRepository, times(1)).findById(id);
        verify(bookStylesRepository, times(1)).save(bookStyles);
    }

    @Test
    void updateBookStyles_ShouldThrowIfNotFound() {
        // Arrange
        logger.info("Тест: updateBookStyles_ShouldThrowIfNotFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.empty());

        BookStyles newData = new BookStyles(id, null, null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookStylesService.updateBookStyles(id, newData));
        verify(bookStylesRepository, times(1)).findById(id);
        verify(bookStylesRepository, never()).save(any(BookStyles.class));
    }

    @Test
    void deleteBookStyles_ShouldDeleteIfFound() {
        // Arrange
        logger.info("Тест: deleteBookStyles_ShouldDeleteIfFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.of(bookStyles));

        // Act
        bookStylesService.deleteBookStyles(id);

        // Assert
        verify(bookStylesRepository, times(1)).findById(id);
        verify(bookStylesRepository, times(1)).delete(bookStyles);
    }

    @Test
    void deleteBookStyles_ShouldThrowIfNotFound() {
        // Arrange
        logger.info("Тест: deleteBookStyles_ShouldThrowIfNotFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookStylesService.deleteBookStyles(id));
        verify(bookStylesRepository, times(1)).findById(id);
        verify(bookStylesRepository, never()).delete(any(BookStyles.class));
    }
}
