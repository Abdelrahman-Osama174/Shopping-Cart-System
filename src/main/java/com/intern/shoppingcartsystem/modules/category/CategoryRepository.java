package com.intern.shoppingcartsystem.modules.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT c FROM Category c WHERE :name IS NULL
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    Page<Category> searchCategory(@Param("name") String name, Pageable pageable);


    @Query("""
        SELECT c FROM Category c WHERE c.active = true
        AND (:name = ''
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))
            OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :name, '%')))
        """)
    Page<Category> searchActiveCategories(@Param("name") String name, Pageable pageable);


    Optional<Category> findByIdAndActiveTrue(Long id);

    List<Category> findAllByParentIsNullAndActiveTrue();

    List<Category> findAllByParentIdAndActiveTrue(Long parentId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByParentId(Long parentId);
}
