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
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент
    @Column(name = "id")
    private Integer id;

    @Column(name = "fio")
    private String fio;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "country")
    private String country;

    @Column(name = "nickname")
    private String nickname;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Authorship> authorships;

}
