package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.username = :loginId OR u.email = :loginId")
    Optional<User> findByUsernameOrEmail(String loginId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true ORDER BY u.fullName ASC")
    java.util.List<User> findActiveUsersByRoleName(String roleName);

}
