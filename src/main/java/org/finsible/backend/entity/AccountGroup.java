package org.finsible.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "account_groups")
@EntityListeners(AuditingEntityListener.class)
public class AccountGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String icon;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSystemDefault = false;

    private Integer displayOrder;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    @CreatedDate  // Called before INSERT
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate // Called before UPDATE
    @Column(nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    private String updatedBy;
}
