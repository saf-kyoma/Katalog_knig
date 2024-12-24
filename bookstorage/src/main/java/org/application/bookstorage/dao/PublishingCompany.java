package org.application.bookstorage.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publishing_companies")
public class PublishingCompany {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "establishment_year")
    private int establishmentYear;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "city")
    private String city;

    @OneToMany(mappedBy = "publishingCompany", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Book> books;
}
