package org.application.bookstorage.service.styles;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Styles;
import org.application.bookstorage.repository.StylesRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StylesServiceImpl implements StylesService {

    private final StylesRepository stylesRepository;

    @Override
    public Styles createStyle(Styles style) {
        return stylesRepository.save(style);
    }

    @Override
    public Optional<Styles> getStyleById(Long id) {
        return stylesRepository.findById(id);
    }

    @Override
    public List<Styles> getAllStyles() {
        return stylesRepository.findAll();
    }

    @Override
    public Styles updateStyle(Long id, Styles styleDetails) {
        Styles style = stylesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Стиль не найден с id " + id));
        style.setName(styleDetails.getName());
        // Обновление других полей при необходимости
        return stylesRepository.save(style);
    }

    @Override
    public void deleteStyle(Long id) {
        Styles style = stylesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Стиль не найден с id " + id));
        stylesRepository.delete(style);
    }
}
