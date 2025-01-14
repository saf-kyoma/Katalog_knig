package org.application.bookstorage.service.authorship;

import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;
import org.application.bookstorage.repository.AuthorshipRepository;
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
class AuthorshipServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorshipServiceTest.class);

    @Mock
    private AuthorshipRepository authorshipRepository;

    @InjectMocks
    private AuthorshipServiceImpl authorshipService;

    private Authorship authorship;
    private AuthorshipId authorshipId;

    @BeforeEach
    void setUp() {
        authorshipId = new AuthorshipId("isbn-123", 10);
        authorship = new Authorship(authorshipId, null, null);
    }

    @Test
    void createAuthorship_ShouldSave() {
        logger.info("Тест: createAuthorship_ShouldSave");
        when(authorshipRepository.save(authorship)).thenReturn(authorship);

        Authorship result = authorshipService.createAuthorship(authorship);

        assertNotNull(result);
        verify(authorshipRepository, times(1)).save(authorship);
    }

    @Test
    void getAuthorshipById_ShouldReturnOptional() {
        logger.info("Тест: getAuthorshipById_ShouldReturnOptional");
        when(authorshipRepository.findById(authorshipId)).thenReturn(Optional.of(authorship));

        Optional<Authorship> result = authorshipService.getAuthorshipById(authorshipId);

        assertTrue(result.isPresent());
        verify(authorshipRepository, times(1)).findById(authorshipId);
    }

    @Test
    void getAllAuthorships_ShouldReturnList() {
        logger.info("Тест: getAllAuthorships_ShouldReturnList");
        List<Authorship> data = Collections.singletonList(authorship);
        when(authorshipRepository.findAll()).thenReturn(data);

        List<Authorship> result = authorshipService.getAllAuthorships();

        assertEquals(1, result.size());
        verify(authorshipRepository, times(1)).findAll();
    }

    @Test
    void updateAuthorship_ShouldUpdateIfExists() {
        logger.info("Тест: updateAuthorship_ShouldUpdateIfExists");
        when(authorshipRepository.findById(authorshipId)).thenReturn(Optional.of(authorship));
        when(authorshipRepository.save(authorship)).thenReturn(authorship);

        Authorship newData = new Authorship(authorshipId, null, null);
        Authorship updated = authorshipService.updateAuthorship(authorshipId, newData);

        assertNotNull(updated);
        verify(authorshipRepository, times(1)).findById(authorshipId);
        verify(authorshipRepository, times(1)).save(authorship);
    }

    @Test
    void updateAuthorship_ShouldThrowIfNotFound() {
        logger.info("Тест: updateAuthorship_ShouldThrowIfNotFound");
        when(authorshipRepository.findById(authorshipId)).thenReturn(Optional.empty());

        Authorship newData = new Authorship(authorshipId, null, null);

        assertThrows(RuntimeException.class, () -> authorshipService.updateAuthorship(authorshipId, newData));
        verify(authorshipRepository, times(1)).findById(authorshipId);
    }

    @Test
    void deleteAuthorship_ShouldDeleteIfExists() {
        logger.info("Тест: deleteAuthorship_ShouldDeleteIfExists");
        when(authorshipRepository.findById(authorshipId)).thenReturn(Optional.of(authorship));

        authorshipService.deleteAuthorship(authorshipId);

        verify(authorshipRepository, times(1)).findById(authorshipId);
        verify(authorshipRepository, times(1)).delete(authorship);
    }

    @Test
    void deleteAuthorship_ShouldThrowIfNotFound() {
        logger.info("Тест: deleteAuthorship_ShouldThrowIfNotFound");
        when(authorshipRepository.findById(authorshipId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authorshipService.deleteAuthorship(authorshipId));
        verify(authorshipRepository, times(1)).findById(authorshipId);
    }
}

