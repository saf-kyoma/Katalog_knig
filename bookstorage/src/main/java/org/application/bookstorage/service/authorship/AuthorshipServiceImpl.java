package org.application.bookstorage.service.authorship;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;
import org.application.bookstorage.repository.AuthorshipRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorshipServiceImpl implements AuthorshipService {

    private final AuthorshipRepository authorshipRepository;

    @Override
    public Authorship createAuthorship(Authorship authorship) {
        return authorshipRepository.save(authorship);
    }

    @Override
    public Optional<Authorship> getAuthorshipById(AuthorshipId id) {
        return authorshipRepository.findById(id);
    }

    @Override
    public List<Authorship> getAllAuthorships() {
        return authorshipRepository.findAll();
    }

    @Override
    public Authorship updateAuthorship(AuthorshipId id, Authorship authorshipDetails) {
        Authorship authorship = authorshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авторство не найдено с id " + id));
        authorship.setBook(authorshipDetails.getBook());
        authorship.setAuthor(authorshipDetails.getAuthor());
        // Обновление других полей при необходимости
        return authorshipRepository.save(authorship);
    }

    @Override
    public void deleteAuthorship(AuthorshipId id) {
        Authorship authorship = authorshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авторство не найдено с id " + id));
        authorshipRepository.delete(authorship);
    }
}
