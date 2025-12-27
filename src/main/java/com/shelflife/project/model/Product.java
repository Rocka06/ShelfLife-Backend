package com.shelflife.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private long ownerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int expirationDaysDelta;

    @Column(nullable = false)
    private int runningLow;

    @Column(unique = true, nullable = true)
    private String barcode;
}
