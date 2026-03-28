package org.finsible.backend.repository;

import org.finsible.backend.dto.response.DailyTransactionSummaryDTO;
import org.finsible.backend.entity.Transaction;
import org.finsible.backend.entity.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.toAccount " +
           "LEFT JOIN FETCH t.fromAccount " +
           "WHERE t.id IN :ids AND t.createdBy = :createdBy")
    List<Transaction> findAllByIdInAndCreatedByWithAccounts(@Param("ids") List<Long> ids,
                                                            @Param("createdBy") String createdBy);

    @Query("SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.toAccount toAcc " +
           "LEFT JOIN FETCH t.fromAccount fromAcc " +
           "LEFT JOIN FETCH t.category " +
           "LEFT JOIN FETCH t.currency " +
           "LEFT JOIN FETCH t.space " +
           "WHERE t.id = :id AND t.createdBy = :createdBy")
    Optional<Transaction> findByIdAndCreatedBy(@Param("id") Long id, @Param("createdBy") String createdBy);

    @Query(value = "SELECT t FROM Transaction t " +
           "LEFT JOIN FETCH t.toAccount toAcc " +
           "LEFT JOIN FETCH t.fromAccount fromAcc " +
           "LEFT JOIN FETCH t.category category " +
           "LEFT JOIN FETCH t.currency " +
            "LEFT JOIN FETCH t.space " +
           "WHERE t.createdBy = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR category.id = :categoryId) " +
           "AND (:accountId IS NULL OR toAcc.id = :accountId OR fromAcc.id = :accountId) " +
           "AND (:accountGroupId IS NULL OR toAcc.accountGroup.id = :accountGroupId OR fromAcc.accountGroup.id = :accountGroupId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:minAmount IS NULL OR t.totalAmount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR t.totalAmount <= :maxAmount) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(t.description) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%') " +
            "OR LOWER(toAcc.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%') " +
            "OR LOWER(fromAcc.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')" +
            "OR LOWER(category.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))",
           countQuery = "SELECT COUNT(t) FROM Transaction t " +
           "LEFT JOIN t.toAccount toAcc " +
           "LEFT JOIN t.fromAccount fromAcc " +
           "LEFT JOIN t.category category " +
           "WHERE t.createdBy = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:accountId IS NULL OR toAcc.id = :accountId OR fromAcc.id = :accountId) " +
           "AND (:accountGroupId IS NULL OR toAcc.accountGroup.id = :accountGroupId OR fromAcc.accountGroup.id = :accountGroupId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:minAmount IS NULL OR t.totalAmount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR t.totalAmount <= :maxAmount) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(t.description) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%') " +
           "OR LOWER(toAcc.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%') " +
           "OR LOWER(fromAcc.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')" +
           "OR LOWER(category.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))")
    Page<Transaction> findAllWithFilters(
            @Param("userId") String userId,
            @Param("type") Type type,
            @Param("categoryId") Long categoryId,
            @Param("accountId") Long accountId,
            @Param("accountGroupId") Long accountGroupId,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT new org.finsible.backend.dto.response.DailyTransactionSummaryDTO(" +
           "t.transactionDate, " +
           "SUM(CASE WHEN t.type = 'INCOME' THEN t.totalAmount ELSE 0 END), " +
           "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.totalAmount ELSE 0 END)) " +
           "FROM Transaction t " +
           "LEFT JOIN t.toAccount toAcc " +
           "LEFT JOIN t.fromAccount fromAcc " +
           "WHERE t.createdBy = :userId " +
           "AND (:type IS NULL OR t.type = :type) AND (:type IS NOT NULL OR t.type IN ('INCOME', 'EXPENSE')) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:accountId IS NULL OR toAcc.id = :accountId OR fromAcc.id = :accountId) " +
           "AND (:accountGroupId IS NULL OR toAcc.accountGroup.id = :accountGroupId OR fromAcc.accountGroup.id = :accountGroupId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "GROUP BY t.transactionDate " +
           "ORDER BY t.transactionDate DESC")
    List<DailyTransactionSummaryDTO> findDailyTransactionSummary(
            @Param("userId") String userId,
            @Param("type") Type type,
            @Param("categoryId") Long categoryId,
            @Param("accountId") Long accountId,
            @Param("accountGroupId") Long accountGroupId,
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );
}
