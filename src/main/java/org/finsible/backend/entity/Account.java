package org.finsible.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // do we need nullable here?
    private User user;  // default cash account is linked to user

    @ManyToOne
    @JoinColumn(name = "account_group_id", nullable = false)
    private AccountGroup accountGroup;

    @Column(nullable = false)
    private String name;

    private String description;
    private String icon;

    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "code", nullable = false)
    private SupportedCurrency currency;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // Indicates if the account is active

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSystemDefault = false; // Indicates if this is the default account for the user

    @CreatedDate  // Called before INSERT
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate // Called before UPDATE
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(columnDefinition = "jsonb")
//    private JsonNode otherFields; // Dynamic fields stored as JSON
}
