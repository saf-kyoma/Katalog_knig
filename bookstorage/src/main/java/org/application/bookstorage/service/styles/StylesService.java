package org.application.bookstorage.service.styles;

import org.application.bookstorage.dao.Styles;

import java.util.List;
import java.util.Optional;

public interface StylesService {
    Styles createStyle(Styles style);
    Optional<Styles> getStyleById(Long id);
    List<Styles> getAllStyles();
    Styles updateStyle(Long id, Styles style);
    void deleteStyle(Long id);
}

