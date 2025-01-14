package org.application.bookstorage.service.book;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.repository.BookRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    @Override
    public Book createBook(Book book) {
        // LOGGING ADDED
        logger.info("Создание книги: {}", book);
        Book saved = bookRepository.save(book);
        logger.info("Книга сохранена: ISBN={}", saved.getIsbn());
        return saved;
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        // LOGGING ADDED
        logger.info("Получение книги по ISBN: {}", isbn);
        return bookRepository.findById(isbn);
    }

    @Override
    public List<Book> getAllBooks(String search, String sortColumn, String sortOrder) {
        // LOGGING ADDED
        logger.info("Получение всех книг (search='{}', sortColumn='{}', sortOrder='{}')", search, sortColumn, sortOrder);

        Sort sort = Sort.unsorted();
        boolean sortByAuthor = false;

        if (sortColumn != null && !sortColumn.isEmpty()) {
            if ("author".equalsIgnoreCase(sortColumn)) {
                sortByAuthor = true;
            } else {
                Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sort = Sort.by(direction, mapSortColumn(sortColumn));
            }
        }

        List<Book> books;
        if (search != null && !search.isEmpty()) {
            // LOGGING ADDED
            logger.info("Поиск книг по названию, содержащему '{}'", search);
            books = bookRepository.findByNameContainingIgnoreCase(search, sort);
        } else {
            books = bookRepository.findAll(sort);
        }

        if (sortByAuthor) {
            // LOGGING ADDED
            logger.info("Дополнительная сортировка по первому автору");
            Comparator<Book> comparator = Comparator.comparing(book -> {
                if (book.getAuthorships() != null && !book.getAuthorships().isEmpty()) {
                    return book.getAuthorships().iterator().next().getAuthor().getFio();
                } else {
                    return "";
                }
            }, String.CASE_INSENSITIVE_ORDER);
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            books.sort(comparator);
        }

        logger.info("Найдено {} книг после сортировки/фильтрации", books.size());
        return books;
    }

    private String mapSortColumn(String sortColumn) {
        switch (sortColumn) {
            case "name":
                return "name";
            case "publication_year":
                return "publicationYear";
            case "publishing_company":
                return "publishingCompany.name";
            case "count_of_books":
                return "countOfBooks";
            case "isbn":
                return "isbn";
            default:
                return "name";
        }
    }

    @Override
    public Book updateBook(String isbn, Book bookDetails) {
        // LOGGING ADDED
        logger.info("Обновление книги ISBN={}. Новые данные: {}", isbn, bookDetails);

        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Книга не найдена с ISBN " + isbn));

        book.setName(bookDetails.getName());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setAgeLimit(bookDetails.getAgeLimit());
        book.setPublishingCompany(bookDetails.getPublishingCompany());
        book.setPageCount(bookDetails.getPageCount());
        book.setLanguage(bookDetails.getLanguage());
        book.setCost(bookDetails.getCost());
        book.setCountOfBooks(bookDetails.getCountOfBooks());

        Book updated = bookRepository.save(book);

        // LOGGING ADDED
        logger.info("Книга ISBN={} успешно обновлена", isbn);
        return updated;
    }

    @Override
    public void deleteBook(String isbn) {
        // LOGGING ADDED
        logger.info("Удаление книги ISBN={}", isbn);

        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Книга не найдена с ISBN " + isbn));
        bookRepository.delete(book);

        // LOGGING ADDED
        logger.info("Книга ISBN={} удалена", isbn);
    }

    @Override
    public void deleteBooks(List<String> isbns) {
        // LOGGING ADDED
        logger.info("Массовое удаление книг: {}", isbns);

        List<Book> books = bookRepository.findAllById(isbns);
        if (books.size() != isbns.size()) {
            // LOGGING ADDED
            logger.warn("Некоторые книги из списка {} не найдены", isbns);
            throw new RuntimeException("Некоторые книги не найдены для удаления.");
        }
        bookRepository.deleteAll(books);

        // LOGGING ADDED
        logger.info("Книги успешно удалены по списку ISBN: {}", isbns);
    }
}
