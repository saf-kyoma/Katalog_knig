package org.application.bookstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "Books_Styles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Style {
    @Id
    @Column(name = "Style")
    private String style;

    @ManyToMany(mappedBy = "styles")
    private Set<Book> books;
}
