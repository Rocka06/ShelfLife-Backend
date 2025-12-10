package com.shelflife.project.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "invalidjwts")
public class InvalidJwt {

    @Id
    @GeneratedValue
    private long id;

    private String token;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;
}
