package org.finsible.backend.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "UserCategories")
@NoArgsConstructor
@AllArgsConstructor
public class UserCategory {

    @EmbeddedId
    private UserCategoryId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // seems we need to change this as this is referenced to one row in categories table.
}
