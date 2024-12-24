package org.application.bookstorage.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authorships")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authorship {

    @EmbeddedId
    private AuthorshipId id;

    @ManyToOne
    @MapsId("bookIsbn")
    @JoinColumn(name = "book_isbn")
    private Book book;

    @ManyToOne
    @MapsId("authorId")
    @JoinColumn(name = "author_id")
    private Author author;
}
