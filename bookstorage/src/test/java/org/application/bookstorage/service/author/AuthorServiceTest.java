package org.application.bookstorage.service.author;

import org.application.bookstorage.dao.Author;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.repository.AuthorRepository;
import org.application.bookstorage.repository.BookRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorServiceTest.class);

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        author1 = new Author(1, "Иванов Иван Иванович", "1990-01-01", "Россия", "ivivan", null);
        author2 = new Author(2, "Петров Пётр Петрович", "1985-05-05", "Россия", "petrov", null);
    }

    @Test
    void createAuthor_ShouldSaveAuthor() {
        // Arrange
        logger.info("Тест: createAuthor_ShouldSaveAuthor");
        when(authorRepository.save(any(Author.class))).thenReturn(author1);

        // Act
        Author savedAuthor = authorService.createAuthor(author1);

        // Assert
        assertNotNull(savedAuthor);
        assertEquals(author1.getId(), savedAuthor.getId());
        verify(authorRepository, times(1)).save(author1);
    }

    @Test
    void getAuthorById_ShouldReturnAuthorIfExists() {
        // Arrange
        logger.info("Тест: getAuthorById_ShouldReturnAuthorIfExists");
        when(authorRepository.findById(1)).thenReturn(Optional.of(author1));

        // Act
        Optional<Author> result = authorService.getAuthorById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(author1.getFio(), result.get().getFio());
        verify(authorRepository, times(1)).findById(1);
    }

    @Test
    void getAuthorById_ShouldReturnEmptyIfNotFound() {
        // Arrange
        logger.info("Тест: getAuthorById_ShouldReturnEmptyIfNotFound");
        when(authorRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorService.getAuthorById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(authorRepository, times(1)).findById(999);
    }

    @Test
    void getAllAuthors_ShouldReturnAllAuthors() {
        // Arrange
        logger.info("Тест: getAllAuthors_ShouldReturnAllAuthors");
        List<Author> authors = Arrays.asList(author1, author2);
        when(authorRepository.findAll()).thenReturn(authors);

        // Act
        List<Author> result = authorService.getAllAuthors();

        // Assert
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    void updateAuthor_ShouldUpdateExistingAuthor() {
        // Arrange
        logger.info("Тест: updateAuthor_ShouldUpdateExistingAuthor");
        when(authorRepository.findById(1)).thenReturn(Optional.of(author1));
        when(authorRepository.save(author1)).thenReturn(author1);

        Author newData = new Author();
        newData.setFio("Обновлённый Автор");

        // Act
        Author updated = authorService.updateAuthor(1, newData);

        // Assert
        assertEquals("Обновлённый Автор", updated.getFio());
        verify(authorRepository, times(1)).findById(1);
        verify(authorRepository, times(1)).save(author1);
    }

    @Test
    void updateAuthor_ShouldThrowExceptionIfNotFound() {
        // Arrange
        logger.info("Тест: updateAuthor_ShouldThrowExceptionIfNotFound");
        when(authorRepository.findById(999)).thenReturn(Optional.empty());

        Author newData = new Author();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorService.updateAuthor(999, newData));
        verify(authorRepository, times(1)).findById(999);
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void deleteAuthor_ShouldDeleteExistingAuthor() {
        // Arrange
        logger.info("Тест: deleteAuthor_ShouldDeleteExistingAuthor");
        when(authorRepository.findById(1)).thenReturn(Optional.of(author1));

        // Act
        authorService.deleteAuthor(1);

        // Assert
        verify(authorRepository, times(1)).findById(1);
        verify(authorRepository, times(1)).delete(author1);
    }

    @Test
    void deleteAuthor_ShouldThrowExceptionIfNotFound() {
        // Arrange
        logger.info("Тест: deleteAuthor_ShouldThrowExceptionIfNotFound");
        when(authorRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authorService.deleteAuthor(999));
        verify(authorRepository, times(1)).findById(999);
        verify(authorRepository, never()).delete(any(Author.class));
    }

    @Test
    void searchAuthorsByFio_ShouldReturnList() {
        // Arrange
        logger.info("Тест: searchAuthorsByFio_ShouldReturnList");
        String fio = "Иван";
        when(authorRepository.findByFioContainingIgnoreCase(fio)).thenReturn(Collections.singletonList(author1));

        // Act
        List<Author> result = authorService.searchAuthorsByFio(fio);

        // Assert
        assertEquals(1, result.size());
        verify(authorRepository, times(1)).findByFioContainingIgnoreCase(fio);
    }

    @Test
    void searchAuthors_ShouldReturnListByFioOrNickname() {
        // Arrange
        logger.info("Тест: searchAuthors_ShouldReturnListByFioOrNickname");
        String query = "ivan";
        when(authorRepository.findByFioContainingIgnoreCaseOrNicknameContainingIgnoreCase(query, query))
                .thenReturn(Collections.singletonList(author1));

        // Act
        List<Author> result = authorService.searchAuthors(query);

        // Assert
        assertEquals(1, result.size());
        verify(authorRepository, times(1))
                .findByFioContainingIgnoreCaseOrNicknameContainingIgnoreCase(query, query);
    }

    @Test
    void deleteAuthors_ShouldDoNothingIfRemoveEverythingFalse() {
        // Arrange
        logger.info("Тест: deleteAuthors_ShouldDoNothingIfRemoveEverythingFalse");

        // Act
        authorService.deleteAuthors(Arrays.asList(1,2), false);

        // Assert
        verify(authorRepository, never()).findAllById(any());
        verify(bookRepository, never()).delete(any(Book.class));
        verify(authorRepository, never()).deleteAll(any());
    }

    @Test
    void deleteAuthors_ShouldDeleteAuthorsAndBooksIfRemoveEverythingTrue() {
        // Arrange
        logger.info("Тест: deleteAuthors_ShouldDeleteAuthorsAndBooksIfRemoveEverythingTrue");

        when(authorRepository.findAllById(Arrays.asList(1,2)))
                .thenReturn(Arrays.asList(author1, author2));

        // Упростим: автору1 приписываем книгу, у автора2 нет связей
        // (Тестовая логика: предполагаем, что после удаления авторов,
        //  если книга осталась без авторов, она будет удалена.)

        // Act
        authorService.deleteAuthors(Arrays.asList(1,2), true);

        // Assert
        verify(authorRepository, times(1)).findAllById(Arrays.asList(1,2));
        verify(bookRepository, times(1)).delete(any(Book.class));
        verify(authorRepository, times(1)).deleteAll(any());
    }
}
