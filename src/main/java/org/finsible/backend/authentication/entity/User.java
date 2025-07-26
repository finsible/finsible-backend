package org.finsible.backend.authentication.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Users")
public class User{

    @Column(name="email")
    private String email;

    @Column(name="name")
    private String name;

    @Column(name="picture")
    private String picture;

    @Id // if we use id without generated value - we need to set it manually
    @Column(name="id")
    private String id;

    private String lastLoggedIn;
    private boolean categoriesEdited;

    private String accountCreated;

    public User(String email, String name, String picture, String id, String lastLoggedIn, boolean categoriesEdited, String accountCreated) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.id = id;
        this.lastLoggedIn = lastLoggedIn;
        this.categoriesEdited = categoriesEdited;
        this.accountCreated = accountCreated;
    }

    public User() { }

}


