package org.application.bookstorage.service.publishingcompany;

import org.application.bookstorage.dao.Book;
import org.application.bookstorage.dao.PublishingCompany;
import org.application.bookstorage.repository.BookRepository;
import org.application.bookstorage.repository.PublishingCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishingCompanyServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(PublishingCompanyServiceTest.class);

    @Mock
    private PublishingCompanyRepository publishingCompanyRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private PublishingCompanyServiceImpl publishingCompanyService;

    private PublishingCompany pc1;

    @BeforeEach
    void setUp() {
        pc1 = new PublishingCompany(
                "MyPublisher",
                LocalDate.of(2000,1,1),
                "Some contacts",
                "Moscow",
                null
        );
    }

    @Test
    void createPublishingCompany_ShouldSave() {
        logger.info("Тест: createPublishingCompany_ShouldSave");
        when(publishingCompanyRepository.save(pc1)).thenReturn(pc1);

        PublishingCompany result = publishingCompanyService.createPublishingCompany(pc1);

        assertNotNull(result);
        verify(publishingCompanyRepository, times(1)).save(pc1);
    }

    @Test
    void getPublishingCompanyByName_ShouldReturnIfFound() {
        logger.info("Тест: getPublishingCompanyByName_ShouldReturnIfFound");
        when(publishingCompanyRepository.findById("MyPublisher")).thenReturn(Optional.of(pc1));

        Optional<PublishingCompany> result = publishingCompanyService.getPublishingCompanyByName("MyPublisher");

        assertTrue(result.isPresent());
        assertEquals("MyPublisher", result.get().getName());
        verify(publishingCompanyRepository, times(1)).findById("MyPublisher");
    }

    @Test
    void getAllPublishingCompanies_ShouldReturnList() {
        logger.info("Тест: getAllPublishingCompanies_ShouldReturnList");
        when(publishingCompanyRepository.findAll()).thenReturn(Collections.singletonList(pc1));

        List<PublishingCompany> result = publishingCompanyService.getAllPublishingCompanies();

        assertEquals(1, result.size());
        verify(publishingCompanyRepository, times(1)).findAll();
    }

    @Test
    void updatePublishingCompany_ShouldUpdateIfSameName() {
        logger.info("Тест: updatePublishingCompany_ShouldUpdateIfSameName");
        when(publishingCompanyRepository.findById("MyPublisher")).thenReturn(Optional.of(pc1));
        when(publishingCompanyRepository.save(pc1)).thenReturn(pc1);

        PublishingCompany newData = new PublishingCompany();
        newData.setName("MyPublisher"); // то же имя
        newData.setCity("Saint-Petersburg");

        PublishingCompany result = publishingCompanyService.updatePublishingCompany("MyPublisher", newData);

        assertEquals("Saint-Petersburg", result.getCity());
        verify(publishingCompanyRepository, times(1)).findById("MyPublisher");
        verify(publishingCompanyRepository, times(1)).save(pc1);
    }

    @Test
    void updatePublishingCompany_ShouldRenameIfDifferentNameAndNotExists() {
        logger.info("Тест: updatePublishingCompany_ShouldRenameIfDifferentNameAndNotExists");
        when(publishingCompanyRepository.findById("MyPublisher")).thenReturn(Optional.of(pc1));
        when(publishingCompanyRepository.existsById("NewName")).thenReturn(false);

        // Мокаем создание новой записи
        PublishingCompany newPC = new PublishingCompany("NewName", null, null, null, null);
        when(publishingCompanyRepository.save(any(PublishingCompany.class))).thenReturn(newPC);

        // Тоже нужно замокать удаление старой
        doNothing().when(publishingCompanyRepository).delete(pc1);

        PublishingCompany updatedData = new PublishingCompany();
        updatedData.setName("NewName");
        updatedData.setCity("Perm");

        PublishingCompany result = publishingCompanyService.updatePublishingCompany("MyPublisher", updatedData);

        assertNotNull(result);
        assertEquals("NewName", result.getName());
        verify(publishingCompanyRepository, times(1)).findById("MyPublisher");
        verify(publishingCompanyRepository, times(1)).existsById("NewName");
        verify(publishingCompanyRepository, times(1)).save(any(PublishingCompany.class));
        verify(publishingCompanyRepository, times(1)).delete(pc1);
    }

    @Test
    void updatePublishingCompany_ShouldThrowIfOldNotFound() {
        logger.info("Тест: updatePublishingCompany_ShouldThrowIfOldNotFound");
        when(publishingCompanyRepository.findById("Wrong")).thenReturn(Optional.empty());

        PublishingCompany updatedData = new PublishingCompany();
        updatedData.setName("NewName");

        assertThrows(RuntimeException.class, () -> publishingCompanyService.updatePublishingCompany("Wrong", updatedData));
        verify(publishingCompanyRepository, times(1)).findById("Wrong");
    }

    @Test
    void deletePublishingCompanies_ShouldDeleteAllIfFound() {
        logger.info("Тест: deletePublishingCompanies_ShouldDeleteAllIfFound");
        when(publishingCompanyRepository.findAllById(Arrays.asList("MyPublisher"))).thenReturn(Collections.singletonList(pc1));

        publishingCompanyService.deletePublishingCompanies(Collections.singletonList("MyPublisher"));

        verify(publishingCompanyRepository, times(1)).findAllById(anyList());
        verify(publishingCompanyRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void deletePublishingCompanies_ShouldThrowIfNotFound() {
        logger.info("Тест: deletePublishingCompanies_ShouldThrowIfNotFound");
        when(publishingCompanyRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () ->
                publishingCompanyService.deletePublishingCompanies(Collections.singletonList("WrongName")));
        verify(publishingCompanyRepository, times(1)).findAllById(anyList());
        verify(publishingCompanyRepository, never()).deleteAll(anyList());
    }

    @Test
    void searchPublishingCompaniesByName_ShouldReturnList() {
        logger.info("Тест: searchPublishingCompaniesByName_ShouldReturnList");
        when(publishingCompanyRepository.findByNameContainingIgnoreCase("Publ"))
                .thenReturn(Collections.singletonList(pc1));

        List<PublishingCompany> result = publishingCompanyService.searchPublishingCompaniesByName("Publ");

        assertEquals(1, result.size());
        verify(publishingCompanyRepository, times(1)).findByNameContainingIgnoreCase("Publ");
    }
}

