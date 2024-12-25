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

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn);
    }

//    @Override
//    public List<Book> getAllBooks() {
//        return bookRepository.findAll();
//    }

    @Override
    public List<Book> getAllBooks(String search, String sortColumn, String sortOrder) {
        Sort sort = Sort.unsorted();
        boolean sortByAuthor = false;

        // Determine sorting
        if (sortColumn != null && !sortColumn.isEmpty()) {
            if ("author".equalsIgnoreCase(sortColumn)) {
                sortByAuthor = true;
            } else {
                Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sort = Sort.by(direction, mapSortColumn(sortColumn));
            }
        }

        // Fetch books based on search and sort
        List<Book> books;
        if (search != null && !search.isEmpty()) {
            books = bookRepository.findByNameContainingIgnoreCase(search, sort);
        } else {
            books = bookRepository.findAll(sort);
        }

        // If sorting by author, perform in-memory sorting
        if (sortByAuthor) {
            Comparator<Book> comparator = Comparator.comparing(book -> {
                if (book.getAuthorships() != null && !book.getAuthorships().isEmpty()) {
                    // Sort by the first author's full name
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

        return books;
    }

    /**
     * Maps frontend sort column names to entity field names.
     *
     * @param sortColumn Frontend column name.
     * @return Entity field name.
     */
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
        // Обновление других полей при необходимости
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Книга не найдена с ISBN " + isbn));
        bookRepository.delete(book);
    }
}

