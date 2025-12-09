package org.finsible.backend.repository;

import org.finsible.backend.entity.Transaction;
import org.finsible.backend.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdAndCreatedBy(Long id, String createdBy);

    List<Transaction> findAllByTypeAndCreatedBy(Type type, String userId);

    List<Transaction> findAllByCategory_IdAndCreatedBy(Long categoryId, String userId);

    List<Transaction> findAllByToAccount_IdAndCreatedBy(Long accountId, String userId);

    List<Transaction> findAllByFromAccount_IdAndCreatedBy(Long accountId, String userId);

    List<Transaction> findAllByToAccount_AccountGroup_IdAndCreatedBy(Long toAccountAccountGroupId, String createdBy);

    List<Transaction> findAllByFromAccount_AccountGroup_IdAndCreatedBy(Long accountGroupId, String userId);
}
