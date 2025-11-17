package org.finsible.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
public class Category {
    public enum CategoryType {
        INCOME, EXPENSE
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @Column(nullable = false)
    private String name;

    private String icon;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    @Column(nullable = false)
    @Builder.Default
    private boolean isSubCategory = false;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory; // if isSubCategory is true, this must be set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    private String updatedBy;

    public boolean isReadOnly() {
        return createdBy == null; // Default categories are read-only
    }

    public boolean canUserModify(User user) {
        return createdBy != null && createdBy.equals(user);
    }
}
