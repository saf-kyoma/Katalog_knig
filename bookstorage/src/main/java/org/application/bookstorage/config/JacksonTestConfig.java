package org.application.bookstorage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonTestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Регистрируем модуль для поддержки Java 8 типов даты/времени (LocalDate, LocalDateTime и т.д.)
        mapper.registerModule(new JavaTimeModule());
        // Отключаем запись дат в виде массивов – мы хотим видеть ISO‑строки (например, "2020-01-01")
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
