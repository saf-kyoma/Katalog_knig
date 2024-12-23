package org.application.bookstorage.dao;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Setter
@EqualsAndHashCode
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fio", nullable = false)
    private String fio;

    @Column(name = "birth_date")
    private Date date;

    @Column(name = "country")
    private String country;

    @Column(name = "nickname")
    private String nickname;

    @ManyToMany
    @JoinTable(
            name = "authorships",
            joinColumns = {@JoinColumn(name = "book_isbn")},
            inverseJoinColumns = {@JoinColumn(name = "author_id")}
    )
    private List<Book> books;
}
