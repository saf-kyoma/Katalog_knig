package org.application.bookstorage.service.authorship;

import org.application.bookstorage.dao.Authorship;
import org.application.bookstorage.dao.AuthorshipId;

import java.util.List;
import java.util.Optional;

public interface AuthorshipService {
    Authorship createAuthorship(Authorship authorship);
    Optional<Authorship> getAuthorshipById(AuthorshipId id);
    List<Authorship> getAllAuthorships();
    Authorship updateAuthorship(AuthorshipId id, Authorship authorship);
    void deleteAuthorship(AuthorshipId id);
}

