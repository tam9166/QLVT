package com.qlvt.repository;

import com.qlvt.entity.AppUser;
import com.qlvt.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    @Query("""
            select u from AppUser u
            where u.deleted = false
              and (lower(u.username) = lower(:lookup) or lower(u.email) = lower(:lookup))
            """)
    Optional<AppUser> findActiveByUsernameOrEmail(@Param("lookup") String lookup);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    List<AppUser> findByDeletedFalseOrderByUsernameAsc();
    List<AppUser> findByDeletedFalseAndUsernameContainingIgnoreCaseOrDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(String username, String fullName, String email);
    long countByDepartmentAndDeletedFalse(String department);
    long countByRoleAndDeletedFalse(UserRole role);
}
