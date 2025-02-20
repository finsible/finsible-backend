package org.finsible.backend.authentication.repository;

import org.finsible.backend.authentication.entity.UserCategory;
import org.finsible.backend.authentication.entity.UserCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCategoryRepository extends JpaRepository<UserCategory, String> {
    void deleteUserCategoriesById(UserCategoryId id);
}
