package org.finsible.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.Instant;

@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User{

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Id // if we use id without generated value - we need to set it manually
    @Column(name="id")
    private String id;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastLoggedIn;

    @CreatedDate
    @Column(nullable = false)
    private Instant accountCreated;

    @Column(nullable = false, name = "categories_edited")
    @Builder.Default
    private boolean isCategoriesEdited = false;

    @ManyToOne
    @JoinColumn(name = "default_language", nullable = false, referencedColumnName = "code")
    private SupportedLanguage defaultLanguage;

    @ManyToOne
    @JoinColumn(name = "default_currency", nullable = false, referencedColumnName = "code")
    private SupportedCurrency defaultCurrency;
}


