package org.finsible.backend.repository;

import org.finsible.backend.entity.AccountGroup;
import org.finsible.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, Long> {
    AccountGroup findAccountGroupByName(String name);

    AccountGroup findAccountGroupById(Long id);

    List<AccountGroup> findByCreatedBy_Id(String createdById);

    List<AccountGroup> findByCreatedBy_IdOrIsSystemDefaultTrueOrderByDisplayOrder(String createdById);
    List<AccountGroup> findByIsSystemDefaultTrue();

    AccountGroup findAccountGroupByIdAndCreatedBy(Long id, User createdBy);
}
