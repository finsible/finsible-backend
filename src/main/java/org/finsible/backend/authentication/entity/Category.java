package org.finsible.backend.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Categories")
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    public enum TransactionType {
        INCOME, EXPENSE
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private String name;

    private Integer color;

    @Column(nullable = false)
    private boolean isDefault;
}
