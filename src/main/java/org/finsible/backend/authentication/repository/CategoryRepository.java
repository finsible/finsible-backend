package org.finsible.backend.authentication.repository;

import org.finsible.backend.authentication.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {
    @Query("SELECT c FROM Category c WHERE c.isDefault = TRUE")
    List<Category> findDefaultCategories();

    @Query("SELECT c FROM Category c " +
            "LEFT JOIN UserCategory uc ON c.id = uc.category.id " +
            "WHERE uc.user.id = :userId")
    List<Category> findUserCategories(@Param("userId") String userId);
}
