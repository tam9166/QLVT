package com.qlvt.repository;

import com.qlvt.entity.AppUser;
import com.qlvt.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    List<AppUser> findByDeletedFalseOrderByUsernameAsc();
    List<AppUser> findByDeletedFalseAndUsernameContainingIgnoreCaseOrDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(String username, String fullName, String email);
    long countByDepartmentAndDeletedFalse(String department);
    long countByRoleAndDeletedFalse(UserRole role);
}
