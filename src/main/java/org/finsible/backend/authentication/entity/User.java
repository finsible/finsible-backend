package org.finsible.backend.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@AllArgsConstructor
@Table(name = "Users")
@NoArgsConstructor
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

    private String accountCreated;

    @Column(nullable = false, name = "categories_edited")
    @Builder.Default
    private boolean isCategoriesEdited = false;

    public User(String email, String name, String picture, String id, String lastLoggedIn, String accountCreated) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.id = id;
        this.lastLoggedIn = lastLoggedIn;
        this.accountCreated = accountCreated;
    }
}


