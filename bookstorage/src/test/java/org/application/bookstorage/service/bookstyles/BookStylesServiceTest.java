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
        id = new BookStylesId("ISBN-123", 1L);
        bookStyles = new BookStyles(id, null, null);
    }

    @Test
    void createBookStyles_ShouldSave() {
        logger.info("Тест: createBookStyles_ShouldSave");
        when(bookStylesRepository.save(bookStyles)).thenReturn(bookStyles);

        BookStyles result = bookStylesService.createBookStyles(bookStyles);

        assertNotNull(result);
        verify(bookStylesRepository, times(1)).save(bookStyles);
    }

    @Test
    void getBookStylesById_ShouldReturnOptional() {
        logger.info("Тест: getBookStylesById_ShouldReturnOptional");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.of(bookStyles));

        Optional<BookStyles> result = bookStylesService.getBookStylesById(id);

        assertTrue(result.isPresent());
        verify(bookStylesRepository, times(1)).findById(id);
    }

    @Test
    void getAllBookStyles_ShouldReturnList() {
        logger.info("Тест: getAllBookStyles_ShouldReturnList");
        List<BookStyles> data = Collections.singletonList(bookStyles);
        when(bookStylesRepository.findAll()).thenReturn(data);

        List<BookStyles> result = bookStylesService.getAllBookStyles();

        assertEquals(1, result.size());
        verify(bookStylesRepository, times(1)).findAll();
    }

    @Test
    void updateBookStyles_ShouldUpdateIfFound() {
        logger.info("Тест: updateBookStyles_ShouldUpdateIfFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.of(bookStyles));
        when(bookStylesRepository.save(bookStyles)).thenReturn(bookStyles);

        BookStyles newData = new BookStyles(id, null, null);
        BookStyles updated = bookStylesService.updateBookStyles(id, newData);

        assertNotNull(updated);
        verify(bookStylesRepository, times(1)).findById(id);
        verify(bookStylesRepository, times(1)).save(bookStyles);
    }

    @Test
    void updateBookStyles_ShouldThrowIfNotFound() {
        logger.info("Тест: updateBookStyles_ShouldThrowIfNotFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.empty());

        BookStyles newData = new BookStyles(id, null, null);

        assertThrows(RuntimeException.class, () -> bookStylesService.updateBookStyles(id, newData));
        verify(bookStylesRepository, times(1)).findById(id);
    }

    @Test
    void deleteBookStyles_ShouldDeleteIfFound() {
        logger.info("Тест: deleteBookStyles_ShouldDeleteIfFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.of(bookStyles));

        bookStylesService.deleteBookStyles(id);

        verify(bookStylesRepository, times(1)).findById(id);
        verify(bookStylesRepository, times(1)).delete(bookStyles);
    }

    @Test
    void deleteBookStyles_ShouldThrowIfNotFound() {
        logger.info("Тест: deleteBookStyles_ShouldThrowIfNotFound");
        when(bookStylesRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookStylesService.deleteBookStyles(id));
        verify(bookStylesRepository, times(1)).findById(id);
    }
}

