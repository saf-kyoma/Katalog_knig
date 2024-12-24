package org.application.bookstorage.service.bookstyles;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;
import org.application.bookstorage.repository.BookStylesRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookStylesServiceImpl implements BookStylesService {

    private final BookStylesRepository bookStylesRepository;

    @Override
    public BookStyles createBookStyles(BookStyles bookStyles) {
        return bookStylesRepository.save(bookStyles);
    }

    @Override
    public Optional<BookStyles> getBookStylesById(BookStylesId id) {
        return bookStylesRepository.findById(id);
    }

    @Override
    public List<BookStyles> getAllBookStyles() {
        return bookStylesRepository.findAll();
    }

    @Override
    public BookStyles updateBookStyles(BookStylesId id, BookStyles bookStylesDetails) {
        BookStyles bookStyles = bookStylesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookStyles не найдены с id " + id));
        bookStyles.setBook(bookStylesDetails.getBook());
        bookStyles.setStyleEntity(bookStylesDetails.getStyleEntity());
        // Обновление других полей при необходимости
        return bookStylesRepository.save(bookStyles);
    }

    @Override
    public void deleteBookStyles(BookStylesId id) {
        BookStyles bookStyles = bookStylesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BookStyles не найдены с id " + id));
        bookStylesRepository.delete(bookStyles);
    }
}
