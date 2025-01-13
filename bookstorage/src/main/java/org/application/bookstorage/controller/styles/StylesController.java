package org.application.bookstorage.controller.styles;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Styles;
import org.application.bookstorage.dto.StylesDTO;
import org.application.bookstorage.service.styles.StylesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/styles")
@RequiredArgsConstructor
public class StylesController {

    private final StylesService stylesService;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(StylesController.class);

    // Создание стиля
    @PostMapping
    public ResponseEntity<StylesDTO> createStyle(@Valid @RequestBody StylesDTO styleDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на создание стиля: {}", styleDTO);

        try {
            Styles style = mapToEntity(styleDTO);
            Styles createdStyle = stylesService.createStyle(style);

            // LOGGING ADDED
            logger.info("Стиль успешно создан: {}", createdStyle.getId());

            StylesDTO responseDTO = mapToDTO(createdStyle);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при создании стиля: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение стиля по ID
    @GetMapping("/{id}")
    public ResponseEntity<StylesDTO> getStyleById(@PathVariable Long id) {
        // LOGGING ADDED
        logger.info("Получен запрос на получение стиля по ID: {}", id);

        return stylesService.getStyleById(id)
                .map(style -> {
                    // LOGGING ADDED
                    logger.info("Стиль найден: {}", style.getId());
                    return new ResponseEntity<>(mapToDTO(style), HttpStatus.OK);
                })
                .orElseGet(() -> {
                    // LOGGING ADDED
                    logger.warn("Стиль с ID {} не найден", id);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    // Получение всех стилей
    @GetMapping
    public ResponseEntity<List<StylesDTO>> getAllStyles() {
        // LOGGING ADDED
        logger.info("Получен запрос на получение всех стилей");

        List<StylesDTO> styles = stylesService.getAllStyles()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("Возвращено {} стилей", styles.size());

        return new ResponseEntity<>(styles, HttpStatus.OK);
    }

    // Обновление стиля
    @PutMapping("/{id}")
    public ResponseEntity<StylesDTO> updateStyle(@PathVariable Long id, @Valid @RequestBody StylesDTO styleDTO) {
        // LOGGING ADDED
        logger.info("Получен запрос на обновление стиля с ID {}. Новые данные: {}", id, styleDTO);

        try {
            Styles styleDetails = mapToEntity(styleDTO);
            Styles updatedStyle = stylesService.updateStyle(id, styleDetails);

            // LOGGING ADDED
            logger.info("Стиль с ID {} успешно обновлён", id);

            StylesDTO responseDTO = mapToDTO(updatedStyle);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при обновлении стиля с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление стиля
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStyle(@PathVariable Long id) {
        // LOGGING ADDED
        logger.info("Получен запрос на удаление стиля с ID {}", id);

        try {
            stylesService.deleteStyle(id);

            // LOGGING ADDED
            logger.info("Стиль с ID {} успешно удалён", id);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            // LOGGING ADDED
            logger.error("Ошибка при удалении стиля с ID {}: {}", id, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Эндпоинт для поиска стилей по части названия
    @GetMapping("/search")
    public ResponseEntity<List<StylesDTO>> searchStyles(@RequestParam("q") String query) {
        // LOGGING ADDED
        logger.info("Получен запрос на поиск стилей по части названия: {}", query);

        List<Styles> styles = stylesService.searchStylesByName(query);
        List<StylesDTO> stylesDTO = styles.stream().map(this::mapToDTO).collect(Collectors.toList());

        // LOGGING ADDED
        logger.info("По запросу '{}' найдено {} стилей", query, stylesDTO.size());

        return new ResponseEntity<>(stylesDTO, HttpStatus.OK);
    }

    // Ручной маппинг DTO в сущность
    private Styles mapToEntity(StylesDTO dto) {
        Styles style = new Styles();
        style.setId(dto.getId());
        style.setName(dto.getName());
        return style;
    }

    // Ручной маппинг сущности в DTO
    private StylesDTO mapToDTO(Styles style) {
        StylesDTO dto = new StylesDTO();
        dto.setId(style.getId());
        dto.setName(style.getName());
        return dto;
    }
}
