package com.example.usermanagement.repository;

import com.example.usermanagement.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
