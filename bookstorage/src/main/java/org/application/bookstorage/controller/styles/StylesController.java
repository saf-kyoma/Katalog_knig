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

@RestController
@RequestMapping("/api/styles")
@RequiredArgsConstructor
public class StylesController {

    private final StylesService stylesService;

    // Создание стиля
    @PostMapping
    public ResponseEntity<StylesDTO> createStyle(@Valid @RequestBody StylesDTO styleDTO) {
        try {
            Styles style = mapToEntity(styleDTO);
            Styles createdStyle = stylesService.createStyle(style);
            StylesDTO responseDTO = mapToDTO(createdStyle);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Получение стиля по ID
    @GetMapping("/{id}")
    public ResponseEntity<StylesDTO> getStyleById(@PathVariable Long id) {
        return stylesService.getStyleById(id)
                .map(style -> new ResponseEntity<>(mapToDTO(style), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Получение всех стилей
    @GetMapping
    public ResponseEntity<List<StylesDTO>> getAllStyles() {
        List<StylesDTO> styles = stylesService.getAllStyles()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(styles, HttpStatus.OK);
    }

    // Обновление стиля
    @PutMapping("/{id}")
    public ResponseEntity<StylesDTO> updateStyle(@PathVariable Long id, @Valid @RequestBody StylesDTO styleDTO) {
        try {
            Styles styleDetails = mapToEntity(styleDTO);
            Styles updatedStyle = stylesService.updateStyle(id, styleDetails);
            StylesDTO responseDTO = mapToDTO(updatedStyle);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Логирование ошибки можно добавить здесь
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Удаление стиля
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStyle(@PathVariable Long id) {
        try {
            stylesService.deleteStyle(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
