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
import java.time.LocalDate;

@Entity
@Table(name = "loan_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LoanDetail {
    @Id
    private Long accountId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "loan_type")
    private String loanType;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "emi_amount")
    private BigDecimal emiAmount;

    @Column(name = "emi_date")
    private Integer emiDate;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "start_date")
    private LocalDate startDate;

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