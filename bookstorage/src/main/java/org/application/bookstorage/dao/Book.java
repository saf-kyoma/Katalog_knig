package org.application.bookstorage.dao;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Fetch;

import java.sql.Date;
import java.util.List;

@Entity
@Data
@Table(name = "books")
public class Book{

    @Id
    @Column(name = "isbn")
    private String isbn;

    @Column(name = "name")
    private String name;

    @Column(name = "publication_year")
    private Date date;

    @Column(name = "age_limit")
    private Double ageLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publishing_company")
    private PublishingCompany publishingCompany;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "language")
    private String language;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "count_of_books")
    private Integer countBook;

    @ManyToMany
    @JoinTable(
            name = "book_styles",
            joinColumns = {@JoinColumn(name = "book_isbn")},
            inverseJoinColumns = {@JoinColumn(name = "style")}
    )
    private List<Styles> styles;

    @ManyToMany(mappedBy = "books")
    private List<Author> authors;
}
