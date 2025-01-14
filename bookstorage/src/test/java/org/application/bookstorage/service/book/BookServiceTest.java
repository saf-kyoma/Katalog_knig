package org.application.bookstorage.service.book;

import org.application.bookstorage.dao.Book;
import org.application.bookstorage.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceTest.class);

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = new Book(
                "ISBN-123",
                "Книга 1",
                LocalDate.of(2020, 1, 1),
                18.0f,
                null,
                300,
                "Russian",
                BigDecimal.valueOf(500.0),
                10,
                null,
                null
        );
        book2 = new Book(
                "ISBN-456",
                "Книга 2",
                LocalDate.of(2021, 2, 2),
                12.0f,
                null,
                200,
                "English",
                BigDecimal.valueOf(300.0),
                5,
                null,
                null
        );
    }

    @Test
    void createBook_ShouldSaveBook() {
        logger.info("Тест: createBook_ShouldSaveBook");
        when(bookRepository.save(book1)).thenReturn(book1);

        Book savedBook = bookService.createBook(book1);

        assertNotNull(savedBook);
        assertEquals(book1.getIsbn(), savedBook.getIsbn());
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void getBookByIsbn_ShouldReturnBookIfExists() {
        logger.info("Тест: getBookByIsbn_ShouldReturnBookIfExists");
        when(bookRepository.findById("ISBN-123")).thenReturn(Optional.of(book1));

        Optional<Book> result = bookService.getBookByIsbn("ISBN-123");

        assertTrue(result.isPresent());
        assertEquals("Книга 1", result.get().getName());
        verify(bookRepository, times(1)).findById("ISBN-123");
    }

    @Test
    void getBookByIsbn_ShouldReturnEmptyIfNotFound() {
        logger.info("Тест: getBookByIsbn_ShouldReturnEmptyIfNotFound");
        when(bookRepository.findById("NON-EXIST")).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookByIsbn("NON-EXIST");

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById("NON-EXIST");
    }

    @Test
    void getAllBooks_ShouldReturnListWithOrWithoutSearch() {
        logger.info("Тест: getAllBooks_ShouldReturnListWithOrWithoutSearch");
        List<Book> books = Arrays.asList(book1, book2);
        when(bookRepository.findAll(Sort.unsorted())).thenReturn(books);

        List<Book> result = bookService.getAllBooks(null, null, null);

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll(Sort.unsorted());
    }

    @Test
    void updateBook_ShouldUpdateIfFound() {
        logger.info("Тест: updateBook_ShouldUpdateIfFound");
        when(bookRepository.findById("ISBN-123")).thenReturn(Optional.of(book1));
        when(bookRepository.save(book1)).thenReturn(book1);

        Book newData = new Book();
        newData.setName("Новое название");
        newData.setAgeLimit(16.0f);

        Book updated = bookService.updateBook("ISBN-123", newData);

        assertEquals("Новое название", updated.getName());
        assertEquals(16.0f, updated.getAgeLimit());
        verify(bookRepository, times(1)).findById("ISBN-123");
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void updateBook_ShouldThrowIfNotFound() {
        logger.info("Тест: updateBook_ShouldThrowIfNotFound");
        when(bookRepository.findById("NOT-FOUND")).thenReturn(Optional.empty());

        Book newData = new Book();

        assertThrows(RuntimeException.class, () -> bookService.updateBook("NOT-FOUND", newData));
        verify(bookRepository, times(1)).findById("NOT-FOUND");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_ShouldDeleteIfFound() {
        logger.info("Тест: deleteBook_ShouldDeleteIfFound");
        when(bookRepository.findById("ISBN-123")).thenReturn(Optional.of(book1));

        bookService.deleteBook("ISBN-123");

        verify(bookRepository, times(1)).findById("ISBN-123");
        verify(bookRepository, times(1)).delete(book1);
    }

    @Test
    void deleteBook_ShouldThrowIfNotFound() {
        logger.info("Тест: deleteBook_ShouldThrowIfNotFound");
        when(bookRepository.findById("NOT-FOUND")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookService.deleteBook("NOT-FOUND"));
        verify(bookRepository, times(1)).findById("NOT-FOUND");
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void deleteBooks_ShouldDeleteAllIfFound() {
        logger.info("Тест: deleteBooks_ShouldDeleteAllIfFound");
        List<String> isbns = Arrays.asList("ISBN-123", "ISBN-456");
        when(bookRepository.findAllById(isbns)).thenReturn(Arrays.asList(book1, book2));

        bookService.deleteBooks(isbns);

        verify(bookRepository, times(1)).findAllById(isbns);
        verify(bookRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void deleteBooks_ShouldThrowIfNotAllFound() {
        logger.info("Тест: deleteBooks_ShouldThrowIfNotAllFound");
        List<String> isbns = Arrays.asList("ISBN-123", "ISBN-999");
        when(bookRepository.findAllById(isbns)).thenReturn(Collections.singletonList(book1));

        assertThrows(RuntimeException.class, () -> bookService.deleteBooks(isbns));
        verify(bookRepository, times(1)).findAllById(isbns);
        verify(bookRepository, never()).deleteAll(anyList());
    }
}

