package org.finsible.backend.repository;

import org.finsible.backend.entity.Category;
import org.finsible.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findCategoriesByCreatedBy_Id(String createdById);

    List<Category> findCategoriesByTypeAndCreatedBy(Category.CategoryType type, User createdBy);
}
