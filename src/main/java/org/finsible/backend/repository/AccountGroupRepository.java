package org.finsible.backend.repository;

import org.finsible.backend.entity.AccountGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, Long> {
    AccountGroup findAccountGroupByName(String name);

    AccountGroup findAccountGroupById(Long id);

    List<AccountGroup> findByCreatedBy_Id(String createdById);

    List<AccountGroup> findByCreatedBy_IdOrIsSystemDefaultTrueOrderByDisplayOrder(String createdById);
    List<AccountGroup> findByIsSystemDefaultTrue();

    AccountGroup findAccountGroupByIdAndCreatedBy_Id(Long id, String createdById);
}
