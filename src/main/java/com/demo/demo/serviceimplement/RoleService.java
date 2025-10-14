package com.demo.demo.serviceimplement;

import com.demo.demo.entities.Role;
import com.demo.demo.entities.RoleName;
import com.demo.demo.repository.RoleRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

   // private final RoleRepository roleRepository;
   private  RoleRepository roleRepository;

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

}
