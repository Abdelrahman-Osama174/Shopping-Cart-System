package com.intern.shoppingcartsystem.modules.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    @Query("""
            SELECT u FROM User u
            WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
            AND (:role IS NULL OR u.role = :role)
            AND (:active IS NULL OR u.active = :active)
            """)
    Page<User> searchUsers(@Param("email") String email, @Param("role") Role role,
            @Param("active") Boolean active, Pageable pageable);

}
