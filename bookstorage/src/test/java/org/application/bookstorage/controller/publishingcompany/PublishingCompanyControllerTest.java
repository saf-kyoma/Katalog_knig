package org.application.bookstorage.controller.publishingcompany;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.application.bookstorage.dao.PublishingCompany;
import org.application.bookstorage.dto.PublishingCompanyDTO;
import org.application.bookstorage.service.publishingcompany.PublishingCompanyService;
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

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PublishingCompanyController.class)
class PublishingCompanyControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(PublishingCompanyControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublishingCompanyService publishingCompanyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createPublishingCompany_ShouldReturnCreated() throws Exception {
        logger.info("Тест контроллера: createPublishingCompany_ShouldReturnCreated");
        PublishingCompanyDTO dto = new PublishingCompanyDTO();
        dto.setName("NewPub");
        dto.setEstablishmentYear(LocalDate.of(1900,1,1));
        dto.setCity("City");
        dto.setContactInfo("Some info");

        PublishingCompany saved = new PublishingCompany("NewPub", LocalDate.of(1900,1,1), "Some info", "City", null);
        when(publishingCompanyService.createPublishingCompany(any(PublishingCompany.class))).thenReturn(saved);

        mockMvc.perform(post("/api/publishing-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NewPub"));
    }

    @Test
    void createPublishingCompany_ShouldReturnBadRequestIfError() throws Exception {
        logger.info("Тест контроллера: createPublishingCompany_ShouldReturnBadRequestIfError");

        when(publishingCompanyService.createPublishingCompany(any(PublishingCompany.class)))
                .thenThrow(new RuntimeException("Error"));

        PublishingCompanyDTO dto = new PublishingCompanyDTO();
        dto.setName("BadPub");

        mockMvc.perform(post("/api/publishing-companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPublishingCompanyByName_ShouldReturnOkIfFound() throws Exception {
        logger.info("Тест контроллера: getPublishingCompanyByName_ShouldReturnOkIfFound");
        PublishingCompany pc = new PublishingCompany("PubName", null, null, null, null);
        when(publishingCompanyService.getPublishingCompanyByName("PubName")).thenReturn(Optional.of(pc));

        mockMvc.perform(get("/api/publishing-companies/PubName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("PubName"));
    }

    @Test
    void getPublishingCompanyByName_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: getPublishingCompanyByName_ShouldReturnNotFoundIfMissing");

        when(publishingCompanyService.getPublishingCompanyByName("NotFound")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/publishing-companies/NotFound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPublishingCompanies_ShouldReturnOk() throws Exception {
        logger.info("Тест контроллера: getAllPublishingCompanies_ShouldReturnOk");

        when(publishingCompanyService.getAllPublishingCompanies()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/publishing-companies"))
                .andExpect(status().isOk());
    }

    @Test
    void updatePublishingCompany_ShouldReturnOkIfUpdated() throws Exception {
        logger.info("Тест контроллера: updatePublishingCompany_ShouldReturnOkIfUpdated");
        PublishingCompanyDTO dto = new PublishingCompanyDTO();
        dto.setName("NewPub");
        dto.setCity("NewCity");

        PublishingCompany updated = new PublishingCompany("NewPub", null, null, "NewCity", null);
        when(publishingCompanyService.updatePublishingCompany(eq("OldPub"), any(PublishingCompany.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/publishing-companies/{originalName}", "OldPub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewPub"))
                .andExpect(jsonPath("$.city").value("NewCity"));
    }

    @Test
    void updatePublishingCompany_ShouldReturnNotFoundIfMissing() throws Exception {
        logger.info("Тест контроллера: updatePublishingCompany_ShouldReturnNotFoundIfMissing");

        when(publishingCompanyService.updatePublishingCompany(eq("WrongPub"), any(PublishingCompany.class)))
                .thenThrow(new RuntimeException("Not found"));

        PublishingCompanyDTO dto = new PublishingCompanyDTO();
        dto.setName("SomePub");

        mockMvc.perform(put("/api/publishing-companies/{originalName}", "WrongPub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePublishingCompaniesBulk_ShouldReturnNoContent() throws Exception {
        logger.info("Тест контроллера: deletePublishingCompaniesBulk_ShouldReturnNoContent");

        List<String> names = Arrays.asList("Pub1", "Pub2");

        mockMvc.perform(delete("/api/publishing-companies/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(names)))
                .andExpect(status().isNoContent());

        verify(publishingCompanyService, times(1)).deletePublishingCompanies(names);
    }

    @Test
    void deletePublishingCompaniesBulk_ShouldReturnNotFoundIfError() throws Exception {
        logger.info("Тест контроллера: deletePublishingCompaniesBulk_ShouldReturnNotFoundIfError");

        doThrow(new RuntimeException("Not found")).when(publishingCompanyService).deletePublishingCompanies(anyList());

        List<String> names = Arrays.asList("Pub1");

        mockMvc.perform(delete("/api/publishing-companies/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(names)))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchPublishingCompanies_ShouldReturnOk() throws Exception {
        logger.info("Тест контроллера: searchPublishingCompanies_ShouldReturnOk");

        PublishingCompany pc = new PublishingCompany("SearchPub", null, null, null, null);
        when(publishingCompanyService.searchPublishingCompaniesByName("Search")).thenReturn(Collections.singletonList(pc));

        mockMvc.perform(get("/api/publishing-companies/search").param("q", "Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("SearchPub"));
    }
}
