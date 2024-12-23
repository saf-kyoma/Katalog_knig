package org.application.bookstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "Authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Id
    @Column(name="\"ID\"")
    private Integer id;

    @Column(name = "FIO")
    private String fio;

    @Column(name = "BirthDate")
    private java.util.Date birthDate;

    @Column(name = "Country")
    private String country;

    @Column(name = "Nickname")
    private String nickname;

    @ManyToMany(mappedBy = "authors")
    private Set<Book> books;
}
