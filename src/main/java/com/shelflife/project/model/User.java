package com.shelflife.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private long id;

    @JsonProperty("email")
    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty("username")
    @Column(nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @JsonProperty("isAdmin")
    private boolean isAdmin;
}
