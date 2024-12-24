package org.application.bookstorage.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_styles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookStyles {

    @EmbeddedId
    private BookStylesId id;

    @ManyToOne
    @MapsId("bookIsbn")
    @JoinColumn(name = "book_isbn")
    private Book book;

    @ManyToOne
    @MapsId("style")
    @JoinColumn(name = "style")
    private Styles styleEntity;
}

