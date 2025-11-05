package com.demo.demo.controllers;

import com.demo.demo.DTO.LoginDto;
import com.demo.demo.entities.RoleName;
import com.demo.demo.entities.UserEntity;
import com.demo.demo.repository.RoleRepository;
import com.demo.demo.repository.UserRepo;
import com.demo.demo.security.JWTGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final RoleRepository roleRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepo userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTGenerator jwtGenerator
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        System.out.println("DEBUG: Login attempt for: " + loginDto.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()
                    )
            );
            System.out.println("DEBUG: Auth SUCCESS");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserEntity user = userRepository.findByUsername(loginDto.getUsername());
            String token = jwtGenerator.generateToken(authentication);
            String page = (user.getRole().getRolename() == RoleName.ADMIN) ? "back" : "front";

            // ✅ Construit l’URL simple selon le username
            String imageUrl = "http://localhost:8082/api/images/" + user.getUsername() + ".png";
            user.setImage(imageUrl);

            Map<String, Object> response = Map.of(
                    "token", token,
                    "user", user,
                    "page", page
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            System.out.println("DEBUG: Auth FAILED: " + e.getMessage());
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
