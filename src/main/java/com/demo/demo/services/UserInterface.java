package com.demo.demo.services;

import com.demo.demo.DTO.RegisterDto;
import com.demo.demo.entities.UserEntity;

import java.util.List;

public interface UserInterface {
    UserEntity adduser(UserEntity user);
    void deletedUser(Long id);
    List<UserEntity> AddListUsers(List<UserEntity> users);
    String addUserWTCP(UserEntity user);
    String addUserWTCP(RegisterDto dto); // méthode avec DTO
    String addUserWTUN(UserEntity user);
    UserEntity UpdateUser(UserEntity user, Long id);
    List<UserEntity> getAllUsers();
    UserEntity getUserById(Long id);
    UserEntity getUserByUsername(String username);
    List<UserEntity> getUserSWT(String un);
    List<UserEntity> getUserByEmail(String un);
}
