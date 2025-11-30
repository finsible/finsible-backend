package org.finsible.backend.repository;

import org.finsible.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAccountsByUser_Id(String userId);

    Account findByIdAndUser_Id(Long id, String userId);
}
