package org.application.bookstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "\"Books\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @Column(name = "\"ISBN\"")
    private String isbn;

    @Column(name = "\"Name\"")
    private String name;

    @Column(name = "PublicationYear")
    private Integer publicationYear;

    @Column(name = "AgeLimit")
    private Float ageLimit;

    @Column(name = "PublishingCompany")
    private String publishingCompany;

    @Column(name = "PageCount")
    private Integer pageCount;

    @Column(name = "\"Language\"")
    private String language;

    @Column(name = "\"Cost\"")
    private Float cost;

    @Column(name = "\"Count\"")
    private Integer count;

    // Связь с жанрами
    @ManyToMany
    @JoinTable(
            name = "Books_Styles",
            joinColumns = @JoinColumn(name = "BookISBN"),
            inverseJoinColumns = @JoinColumn(name = "Style")
    )
    private Set<Style> styles;

    // Связь с авторами через Authorships
    @ManyToMany
    @JoinTable(
            name = "Authorships",
            joinColumns = @JoinColumn(name = "BookISBN"),
            inverseJoinColumns = @JoinColumn(name = "AuthorID")
    )
    private Set<Author> authors;
}
