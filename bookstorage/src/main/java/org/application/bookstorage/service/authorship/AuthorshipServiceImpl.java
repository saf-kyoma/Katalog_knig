package org.application.bookstorage.service.authorship;

import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;
import org.application.bookstorage.repository.AuthorshipRepository;
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
public class AuthorshipServiceImpl implements AuthorshipService {

    private final AuthorshipRepository authorshipRepository;

    // LOGGING ADDED
    private static final Logger logger = LoggerFactory.getLogger(AuthorshipServiceImpl.class);

    @Override
    public Authorship createAuthorship(Authorship authorship) {
        // LOGGING ADDED
        logger.info("Создание авторства: {}", authorship);
        return authorshipRepository.save(authorship);
    }

    @Override
    public Optional<Authorship> getAuthorshipById(AuthorshipId id) {
        // LOGGING ADDED
        logger.info("Получение авторства по ключу: {}", id);
        return authorshipRepository.findById(id);
    }

    @Override
    public List<Authorship> getAllAuthorships() {
        // LOGGING ADDED
        logger.info("Получение списка всех авторств");
        List<Authorship> result = authorshipRepository.findAll();
        logger.info("Найдено {} авторств", result.size());
        return result;
    }

    @Override
    public Authorship updateAuthorship(AuthorshipId id, Authorship authorshipDetails) {
        // LOGGING ADDED
        logger.info("Обновление авторства с ключом {}. Новые данные: {}", id, authorshipDetails);

        Authorship authorship = authorshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авторство не найдено с id " + id));
        authorship.setBook(authorshipDetails.getBook());
        authorship.setAuthor(authorshipDetails.getAuthor());

        Authorship updated = authorshipRepository.save(authorship);

        // LOGGING ADDED
        logger.info("Авторство с ключом {} успешно обновлено", id);
        return updated;
    }

    @Override
    public void deleteAuthorship(AuthorshipId id) {
        // LOGGING ADDED
        logger.info("Удаление авторства с ключом {}", id);

        Authorship authorship = authorshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авторство не найдено с id " + id));
        authorshipRepository.delete(authorship);

        // LOGGING ADDED
        logger.info("Авторство с ключом {} удалено", id);
    }
}
