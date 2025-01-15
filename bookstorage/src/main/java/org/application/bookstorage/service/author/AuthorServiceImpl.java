package org.application.bookstorage.service.author;

import lombok.AllArgsConstructor;
import org.application.bookstorage.dao.Author;
import org.application.bookstorage.dao.Book;
import org.application.bookstorage.repository.AuthorRepository;
import org.application.bookstorage.repository.BookRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(AuthorServiceImpl.class);

    @Override
    public Author createAuthor(Author author) {
        // LOGGING ADDED
        logger.info("Создание автора: {}", author);

        Author saved = authorRepository.save(author);

        // LOGGING ADDED
        logger.info("Автор сохранён с ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<Author> getAuthorById(int id) {
        // LOGGING ADDED
        logger.info("Получение автора по ID: {}", id);
        return authorRepository.findById(id);
    }

    @Override
    public List<Author> getAllAuthors() {
        // LOGGING ADDED
        logger.info("Получение списка всех авторов");
        List<Author> authors = authorRepository.findAll();

        // LOGGING ADDED
        logger.info("Найдено {} авторов", authors.size());
        return authors;
    }

    @Override
    public Author updateAuthor(int id, Author authorDetails) {
        // LOGGING ADDED
        logger.info("Обновление автора с ID: {}. Новые данные: {}", id, authorDetails);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден с id " + id));

        author.setFio(authorDetails.getFio());
        author.setBirthDate(authorDetails.getBirthDate());
        author.setCountry(authorDetails.getCountry());
        author.setNickname(authorDetails.getNickname());

        Author updated = authorRepository.save(author);

        // LOGGING ADDED
        logger.info("Автор с ID {} успешно обновлён", id);
        return updated;
    }

    @Override
    public void deleteAuthor(int id) {
        // LOGGING ADDED
        logger.info("Удаление автора с ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден с id " + id));
        authorRepository.delete(author);

        // LOGGING ADDED
        logger.info("Автор с ID {} удалён", id);
    }

    @Override
    public List<Author> searchAuthorsByFio(String fio) {
        // LOGGING ADDED
        logger.info("Поиск авторов по ФИО (ignoring case) содержащему: {}", fio);
        return authorRepository.findByFioContainingIgnoreCase(fio);
    }

    @Override
    public List<Author> searchAuthors(String query) {
        // LOGGING ADDED
        logger.info("Поиск авторов по ФИО или псевдониму содержащим: {}", query);
        return authorRepository.findByFioContainingIgnoreCaseOrNicknameContainingIgnoreCase(query, query);
    }

    @Override
    public void deleteAuthors(List<Integer> authorIds, boolean removeEverything) {
        // LOGGING ADDED
        logger.info("Удаление авторов по списку ID: {}. Параметр removeEverything={}", authorIds, removeEverything);

        if (!removeEverything) {
            // LOGGING ADDED
            logger.info("removeEverything=false -> удаление не выполняется");
            return;
        }

        List<Author> authors = authorRepository.findAllById(authorIds);
        if (authors.size() != authorIds.size()) {
            // LOGGING ADDED
            logger.warn("Некоторые авторы из списка {} не найдены", authorIds);
            throw new RuntimeException("Некоторые авторы не найдены для удаления.");
        }

        // Сначала собираем книги, принадлежащие этим авторам
        Set<Book> booksToCheck = new HashSet<>();
        for (Author author : authors) {
            if (author.getAuthorships() != null) {
                author.getAuthorships().forEach(a -> booksToCheck.add(a.getBook()));
            }
        }

        // Удаляем книги, у которых после удаления авторов больше не останется авторов
        for (Book book : booksToCheck) {
            int totalAuthors = book.getAuthorships().size();
            long authorsToRemoveInThisBook = book.getAuthorships().stream()
                    .filter(a -> authorIds.contains(a.getAuthor().getId()))
                    .count();
            if (totalAuthors == authorsToRemoveInThisBook) {
                // LOGGING ADDED
                logger.info("Книга ISBN={} не будет иметь авторов после удаления, удаляем книгу.", book.getIsbn());
                bookRepository.delete(book);
            }
        }

        // Удаляем авторов
        authorRepository.deleteAll(authors);

        // LOGGING ADDED
        logger.info("Авторы успешно удалены по списку ID: {}", authorIds);
    }
}
