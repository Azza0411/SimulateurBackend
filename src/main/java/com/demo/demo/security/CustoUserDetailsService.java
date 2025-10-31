package com.demo.demo.security;

import com.demo.demo.entities.Role;
import com.demo.demo.entities.UserEntity;
import com.demo.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustoUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;
    @Autowired
    public CustoUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepo.findByUsername(username);
        if (user == null) {
            System.out.println("DEBUG: User NULL - Not found!");
            throw new UsernameNotFoundException("User not found");}
        System.out.println("DEBUG: User found, password starts with: " + user.getPassword().substring(0, 10));
        System.out.println("DEBUG: Role: " + (user.getRole() != null ? user.getRole().getRolename() : "NULL"));
        return new User(user.getUsername(),user.getPassword(), mapRolesToAuthorities(user.getRole()));

    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(Role userRole){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority((userRole.getRolename().toString())));
        return authorities;
    }}
