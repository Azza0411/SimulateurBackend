package com.demo.demo.serviceimplement;

import com.demo.demo.DTO.RegisterDto;
import com.demo.demo.entities.Role;
import com.demo.demo.entities.RoleName;
import com.demo.demo.entities.UserEntity;
import com.demo.demo.repository.RoleRepository;
import com.demo.demo.repository.UserRepo;
import com.demo.demo.services.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserImplement implements UserInterface {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserEntity adduser(UserEntity user) {
        user.setDateInscription(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword())); // encode password
        return userRepo.save(user);
    }

    @Override
    public void deletedUser(Long id) {
        userRepo.deleteById(id);
    }

    @Override
    public List<UserEntity> AddListUsers(List<UserEntity> users) {
        users.forEach(u -> {
            u.setDateInscription(new Date());
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        });
        return userRepo.saveAll(users);
    }

    @Override
    public String addUserWTCP(UserEntity user) {
        return null;
    }

    @Override
    public String addUserWTCP(RegisterDto dto) {
        try {
            UserEntity user = new UserEntity();
            user.setFirstName(dto.getFirstname());
            user.setLastName(dto.getLastname());
            user.setEmail(dto.getEmail());
            user.setUsername(dto.getUsername());

            // 🔹 Encodage du mot de passe
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                return "Le mot de passe et la confirmation ne correspondent pas";
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            user.setConfirmPassword(dto.getConfirmPassword()); // optionnel
            user.setCin(dto.getCin());
            user.setTelephone(dto.getTelephone());
            user.setAge(dto.getAge());
            user.setAddress(dto.getAddress());
            user.setImage(dto.getImageUrl());
            user.setDateInscription(new Date());

            // Récupération du rôle
            Role role = roleRepo.findByRolename(RoleName.valueOf(dto.getRoleName().toUpperCase()))
                    .orElseThrow(() -> new RuntimeException("Role introuvable : " + dto.getRoleName()));
            user.setRole(role);

            userRepo.save(user);
            return "Utilisateur créé avec succès";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la création du compte : " + e.getMessage();
        }
    }

    @Override
    public String addUserWTUN(UserEntity user) {
        if (userRepo.existsByUsername(user.getUsername())) {
            return "Utilisateur existe déjà";
        }
        user.setDateInscription(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return "Utilisateur créé avec succès";
    }

    @Override
    public UserEntity UpdateUser(UserEntity user, Long id) {
        UserEntity u = userRepo.findById(id).orElse(null);
        if (u != null) {
            u.setFirstName(user.getFirstName());
            u.setLastName(user.getLastName());
            u.setEmail(user.getEmail());
            u.setUsername(user.getUsername());
            u.setAddress(user.getAddress());
            u.setAge(user.getAge());
            u.setTelephone(user.getTelephone());
            u.setCin(user.getCin());
            u.setImage(user.getImage());
            u.setRole(user.getRole());
            if(user.getPassword() != null) {
                u.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return userRepo.save(u);
        }
        return null;
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public List<UserEntity> getUserSWT(String un) {
        return userRepo.findbycle(un);
    }

    @Override
    public List<UserEntity> getUserByEmail(String un) {
        return userRepo.findbydomaine(un);
    }
}
