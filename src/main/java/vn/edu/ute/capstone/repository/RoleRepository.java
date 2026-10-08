package vn.edu.ute.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ute.capstone.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
