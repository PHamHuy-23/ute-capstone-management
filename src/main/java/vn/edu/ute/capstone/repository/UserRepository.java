package vn.edu.ute.capstone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.ute.capstone.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :loginId OR u.email = :loginId")
    Optional<User> findByUsernameOrEmail(String loginId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true ORDER BY u.fullName ASC")
    java.util.List<User> findActiveUsersByRoleName(String roleName);

    // Phân trang toàn bộ người dùng
    Page<User> findAll(Pageable pageable);

    // Phân trang người dùng lọc theo Khoa (Faculty Isolation)
    @Query("SELECT u FROM User u WHERE u.department.faculty.id = :facultyId")
    Page<User> findByFacultyId(@Param("facultyId") Long facultyId, Pageable pageable);

    // Lọc danh sách theo Khoa
    @Query("SELECT u FROM User u WHERE u.department.faculty.id = :facultyId")
    java.util.List<User> findByFacultyId(@Param("facultyId") Long facultyId);

}
