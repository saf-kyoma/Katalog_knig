package org.application.bookstorage.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "publishing_companies")
public class PublishingCompany {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "establishment_year")
    private Integer establishmentYear;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "city")
    private String city;
}
