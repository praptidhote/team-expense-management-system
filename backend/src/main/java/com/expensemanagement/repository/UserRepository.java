package com.expensemanagement.repository;

import com.expensemanagement.entity.Role;
import com.expensemanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findActiveUsersByRoleName(@Param("roleName") com.expensemanagement.enums.RoleName roleName);

    List<User> findByManagerId(Long managerId);

    Page<User> findAll(Pageable pageable);

    long countByRolesContaining(Role role);
}
