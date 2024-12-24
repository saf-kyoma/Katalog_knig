package org.application.bookstorage.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table (name = "books")
public class Book {
     @Id
     @Column(name = "isbn")
     private String isbn;

     @Column(name = "name")
     private String name;

     @Column(name = "publication_year")
     private String publicationYear; // Изменено на String для соответствия SQL типу date

     @Column(name = "age_limit")
     private float ageLimit; // Изменено на float для соответствия SQL типу real

     @ManyToOne
     @JoinColumn(name = "publishing_company")
     private PublishingCompany publishingCompany;

     @Column(name = "page_count")
     private int pageCount;

     @Column(name = "language")
     private String language;

     @Column(name = "cost")
     private float cost;

     @Column(name = "count_of_books")
     private int countOfBooks;

     @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
     private Set<Authorship> authorships;

     @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
     private Set<BookStyles> bookStyles;

}
