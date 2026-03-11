package org.finsible.backend.repository;

import org.finsible.backend.entity.Category;
import org.finsible.backend.entity.Type;
import org.finsible.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parentCategory " +
           "WHERE c.createdBy.id = :createdById")
    List<Category> findCategoriesByCreatedBy_Id(@Param("createdById") String createdById);

    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parentCategory " +
           "WHERE c.type = :type " +
           "AND (c.createdBy IS NULL OR c.createdBy = :createdBy)")
    List<Category> findCategoriesForUserByType(@Param("type") Type type, @Param("createdBy") User createdBy);

    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parentCategory " +
           "WHERE c.id = :id AND (c.createdBy IS NULL OR c.createdBy.id = :createdById)")
    Optional<Category> findByIdAndCreatedBy_Id(@Param("id") Long id, @Param("createdById") String createdById);

    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parentCategory " +
           "WHERE c.id = :id AND c.type = :type")
    Category findCategoryByIdAndType(@Param("id") Long id, @Param("type") Type type);
}
