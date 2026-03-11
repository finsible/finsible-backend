package org.finsible.backend.repository;

import org.finsible.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a " +
           "LEFT JOIN FETCH a.accountGroup " +
           "LEFT JOIN FETCH a.currency " +
           "WHERE a.user.id = :userId")
    List<Account> findAccountsByUser_Id(@Param("userId") String userId);

    @Query("SELECT a FROM Account a " +
           "LEFT JOIN FETCH a.accountGroup " +
           "LEFT JOIN FETCH a.currency " +
           "WHERE a.id = :id AND a.user.id = :userId")
    Optional<Account> findByIdAndUser_Id(@Param("id") Long id, @Param("userId") String userId);
}
