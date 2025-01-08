package org.application.bookstorage.service.author;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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

@Service
@AllArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Override
    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Override
    public Optional<Author> getAuthorById(int id) {
        return authorRepository.findById(id);
    }

    @Override
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @Override
    public Author updateAuthor(int id, Author authorDetails) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден с id " + id));
        author.setFio(authorDetails.getFio());
        author.setBirthDate(authorDetails.getBirthDate());
        author.setCountry(authorDetails.getCountry());
        author.setNickname(authorDetails.getNickname());
        // Обновление других полей при необходимости
        return authorRepository.save(author);
    }

    @Override
    public void deleteAuthor(int id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автор не найден с id " + id));
        authorRepository.delete(author);
    }

    @Override
    public List<Author> searchAuthorsByFio(String fio) {
        return authorRepository.findByFioContainingIgnoreCase(fio);
    }

    @Override
    public List<Author> searchAuthors(String query) {
        return authorRepository.findByFioContainingIgnoreCaseOrNicknameContainingIgnoreCase(query, query);
    }

    public void deleteAuthors(List<Integer> authorIds, boolean removeEverything) {
        // Если removeEverything = false, ничего не делаем
        if (!removeEverything) {
            return; // просто выходим, не удаляя ни авторов, ни книги
        }

        // Если removeEverything = true, удаляем авторов
        List<Author> authors = authorRepository.findAllById(authorIds);
        if (authors.size() != authorIds.size()) {
            throw new RuntimeException("Некоторые авторы не найдены для удаления.");
        }

        // Сначала собираем все книги, которые потенциально принадлежат этим авторам
        Set<Book> booksToCheck = new HashSet<>();
        for (Author author : authors) {
            if (author.getAuthorships() != null) {
                author.getAuthorships().forEach(a -> booksToCheck.add(a.getBook()));
            }
        }

        // Для каждой книги проверяем, останутся ли у неё авторы
        for (Book book : booksToCheck) {
            int totalAuthors = book.getAuthorships().size(); // общее кол-во авторов у книги
            long authorsToRemoveInThisBook = book.getAuthorships().stream()
                    .filter(a -> authorIds.contains(a.getAuthor().getId()))
                    .count();
            // Если после удаления авторов у книги не останется ни одного автора:
            if (totalAuthors == authorsToRemoveInThisBook) {
                // Удаляем саму книгу
                bookRepository.delete(book);
            }
        }

        // И в конце — удаляем авторов
        authorRepository.deleteAll(authors);
    }
}
