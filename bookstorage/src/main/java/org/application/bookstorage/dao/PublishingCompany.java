package org.application.bookstorage.dao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private LocalDate establishmentYear;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "city")
    private String city;

    @OneToMany(mappedBy = "publishingCompany", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<Book> books;
}

