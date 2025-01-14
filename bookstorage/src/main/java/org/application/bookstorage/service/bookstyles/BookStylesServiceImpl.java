package org.application.bookstorage.service.bookstyles;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.application.bookstorage.repository.BookStylesRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class BookStylesServiceImpl implements BookStylesService {

    private final BookStylesRepository bookStylesRepository;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(BookStylesServiceImpl.class);

    @Override
    public BookStyles createBookStyles(BookStyles bookStyles) {
        // LOGGING ADDED
        logger.info("Создание связи BookStyles: {}", bookStyles);
        return bookStylesRepository.save(bookStyles);
    }

    @Override
    public Optional<BookStyles> getBookStylesById(BookStylesId id) {
        // LOGGING ADDED
        logger.info("Получение связи BookStyles по ключу: {}", id);
        return bookStylesRepository.findById(id);
    }

    @Override
    public List<BookStyles> getAllBookStyles() {
        // LOGGING ADDED
        logger.info("Получение списка всех BookStyles");
        List<BookStyles> list = bookStylesRepository.findAll();
        logger.info("Найдено {} записей BookStyles", list.size());
        return list;
    }

    @Override
    public BookStyles updateBookStyles(BookStylesId id, BookStyles bookStylesDetails) {
        // LOGGING ADDED
        logger.info("Обновление BookStyles с ключом {}. Новые данные: {}", id, bookStylesDetails);

        BookStyles bookStyles = bookStylesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookStyles не найдены с id " + id));

        bookStyles.setBook(bookStylesDetails.getBook());
        bookStyles.setStyleEntity(bookStylesDetails.getStyleEntity());

        BookStyles updated = bookStylesRepository.save(bookStyles);

        // LOGGING ADDED
        logger.info("BookStyles с ключом {} успешно обновлён", id);
        return updated;
    }

    @Override
    public void deleteBookStyles(BookStylesId id) {
        // LOGGING ADDED
        logger.info("Удаление BookStyles с ключом {}", id);

        BookStyles bookStyles = bookStylesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookStyles не найдены с id " + id));
        bookStylesRepository.delete(bookStyles);

        // LOGGING ADDED
        logger.info("BookStyles с ключом {} удалён", id);
    }
}
