package org.application.bookstorage.service.bookstyles;

import org.application.bookstorage.dao.BookStyles;
import org.application.bookstorage.dao.BookStylesId;

import java.util.List;
import java.util.Optional;

public interface BookStylesService {
    BookStyles createBookStyles(BookStyles bookStyles);
    Optional<BookStyles> getBookStylesById(BookStylesId id);
    List<BookStyles> getAllBookStyles();
    BookStyles updateBookStyles(BookStylesId id, BookStyles bookStyles);
    void deleteBookStyles(BookStylesId id);
}

