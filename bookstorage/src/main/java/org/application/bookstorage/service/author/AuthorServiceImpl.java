package org.application.bookstorage.service.author;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Author;
import org.application.bookstorage.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private AuthorRepository authorRepository;

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
}
