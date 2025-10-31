package com.demo.demo.repository;

import com.demo.demo.entities.Role;
import com.demo.demo.entities.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> { // Long au lieu de Integer

    // ✅ Le nom du champ dans l'entity est "rolename"
    Optional<Role> findByRolename(RoleName rolename);

}
