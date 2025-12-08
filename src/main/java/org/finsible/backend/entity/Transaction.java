package org.finsible.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;

    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Long transactionDate; // store in milliseconds since epoch

    private BigDecimal userShare; // Amount paid by the user

    @Builder.Default
    private boolean isSplit = false;

    @ManyToOne
    private User paidBy; // User who paid the amount

    @ManyToOne
    private Category category;

    @ManyToOne
    private Account toAccount; // used in income and transfer

    @ManyToOne
    private Account fromAccount; // used in expense and transfer

    private String description;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "code", nullable = false)
    private SupportedCurrency currency;

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
}
