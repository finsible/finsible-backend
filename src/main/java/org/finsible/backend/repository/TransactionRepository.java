package org.finsible.backend.repository;

import org.finsible.backend.entity.Transaction;
import org.finsible.backend.entity.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdAndCreatedBy(Long id, String createdBy);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN t.toAccount toAcc " +
           "LEFT JOIN t.fromAccount fromAcc " +
           "WHERE t.createdBy = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:accountId IS NULL OR toAcc.id = :accountId OR fromAcc.id = :accountId) " +
           "AND (:accountGroupId IS NULL OR toAcc.accountGroup.id = :accountGroupId OR fromAcc.accountGroup.id = :accountGroupId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<Transaction> findAllWithFilters(
            @Param("userId") String userId,
            @Param("type") Type type,
            @Param("categoryId") Long categoryId,
            @Param("accountId") Long accountId,
            @Param("accountGroupId") Long accountGroupId,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate,
            Pageable pageable
    );
}
