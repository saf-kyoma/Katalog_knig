package org.application.bookstorage.service.styles;

import org.application.bookstorage.dao.Styles;
import org.application.bookstorage.repository.StylesRepository;
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
class StylesServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(StylesServiceTest.class);

    @Mock
    private StylesRepository stylesRepository;

    @InjectMocks
    private StylesServiceImpl stylesService;

    private Styles style1;

    @BeforeEach
    void setUp() {
        style1 = new Styles(1L, "Ужасы", null);
    }

    @Test
    void createStyle_ShouldSave() {
        logger.info("Тест: createStyle_ShouldSave");
        when(stylesRepository.save(style1)).thenReturn(style1);

        Styles result = stylesService.createStyle(style1);

        assertNotNull(result);
        verify(stylesRepository, times(1)).save(style1);
    }

    @Test
    void getStyleById_ShouldReturnIfExists() {
        logger.info("Тест: getStyleById_ShouldReturnIfExists");
        when(stylesRepository.findById(1L)).thenReturn(Optional.of(style1));

        Optional<Styles> result = stylesService.getStyleById(1L);

        assertTrue(result.isPresent());
        assertEquals("Ужасы", result.get().getName());
        verify(stylesRepository, times(1)).findById(1L);
    }

    @Test
    void getAllStyles_ShouldReturnList() {
        logger.info("Тест: getAllStyles_ShouldReturnList");
        when(stylesRepository.findAll()).thenReturn(Collections.singletonList(style1));

        List<Styles> result = stylesService.getAllStyles();

        assertEquals(1, result.size());
        verify(stylesRepository, times(1)).findAll();
    }

    @Test
    void updateStyle_ShouldUpdateIfFound() {
        logger.info("Тест: updateStyle_ShouldUpdateIfFound");
        when(stylesRepository.findById(1L)).thenReturn(Optional.of(style1));
        when(stylesRepository.save(style1)).thenReturn(style1);

        Styles newData = new Styles();
        newData.setName("Комедия");

        Styles updated = stylesService.updateStyle(1L, newData);

        assertEquals("Комедия", updated.getName());
        verify(stylesRepository, times(1)).findById(1L);
        verify(stylesRepository, times(1)).save(style1);
    }

    @Test
    void updateStyle_ShouldThrowIfNotFound() {
        logger.info("Тест: updateStyle_ShouldThrowIfNotFound");
        when(stylesRepository.findById(999L)).thenReturn(Optional.empty());

        Styles newData = new Styles();

        assertThrows(RuntimeException.class, () -> stylesService.updateStyle(999L, newData));
        verify(stylesRepository, times(1)).findById(999L);
        verify(stylesRepository, never()).save(any(Styles.class));
    }

    @Test
    void deleteStyle_ShouldDeleteIfFound() {
        logger.info("Тест: deleteStyle_ShouldDeleteIfFound");
        when(stylesRepository.findById(1L)).thenReturn(Optional.of(style1));

        stylesService.deleteStyle(1L);

        verify(stylesRepository, times(1)).findById(1L);
        verify(stylesRepository, times(1)).delete(style1);
    }

    @Test
    void deleteStyle_ShouldThrowIfNotFound() {
        logger.info("Тест: deleteStyle_ShouldThrowIfNotFound");
        when(stylesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> stylesService.deleteStyle(999L));
        verify(stylesRepository, times(1)).findById(999L);
        verify(stylesRepository, never()).delete(any(Styles.class));
    }

    @Test
    void getStyleByName_ShouldReturnExactIfExists() {
        logger.info("Тест: getStyleByName_ShouldReturnExactIfExists");
        when(stylesRepository.findByNameIgnoreCase("Ужасы")).thenReturn(Optional.of(style1));

        Optional<Styles> result = stylesService.getStyleByName("Ужасы");

        assertTrue(result.isPresent());
        assertEquals("Ужасы", result.get().getName());
        verify(stylesRepository, times(1)).findByNameIgnoreCase("Ужасы");
    }

    @Test
    void getStyleByName_ShouldReturnPartialIfNoExact() {
        logger.info("Тест: getStyleByName_ShouldReturnPartialIfNoExact");
        when(stylesRepository.findByNameIgnoreCase("Комедия")).thenReturn(Optional.empty());
        when(stylesRepository.findByNameContainingIgnoreCase("Комедия"))
                .thenReturn(Collections.singletonList(style1));

        Optional<Styles> result = stylesService.getStyleByName("Комедия");

        assertTrue(result.isPresent());
        assertEquals(style1, result.get());
        verify(stylesRepository, times(1)).findByNameIgnoreCase("Комедия");
        verify(stylesRepository, times(1)).findByNameContainingIgnoreCase("Комедия");
    }

    @Test
    void getStyleByName_ShouldReturnEmptyIfNoMatches() {
        logger.info("Тест: getStyleByName_ShouldReturnEmptyIfNoMatches");
        when(stylesRepository.findByNameIgnoreCase("Foo")).thenReturn(Optional.empty());
        when(stylesRepository.findByNameContainingIgnoreCase("Foo")).thenReturn(Collections.emptyList());

        Optional<Styles> result = stylesService.getStyleByName("Foo");

        assertFalse(result.isPresent());
        verify(stylesRepository, times(1)).findByNameIgnoreCase("Foo");
        verify(stylesRepository, times(1)).findByNameContainingIgnoreCase("Foo");
    }

    @Test
    void searchStylesByName_ShouldReturnList() {
        logger.info("Тест: searchStylesByName_ShouldReturnList");
        when(stylesRepository.findByNameContainingIgnoreCase("уж")).thenReturn(Collections.singletonList(style1));

        List<Styles> result = stylesService.searchStylesByName("уж");

        assertEquals(1, result.size());
        verify(stylesRepository, times(1)).findByNameContainingIgnoreCase("уж");
    }
}

