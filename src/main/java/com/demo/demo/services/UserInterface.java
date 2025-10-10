package com.demo.demo.services;

import com.demo.demo.entities.RoleName;
import com.demo.demo.entities.UserEntity;

import java.util.List;
import java.util.Set;

public interface UserInterface {
    UserEntity adduser(UserEntity user);
    void deletedUser(Long id);
    List<UserEntity> AddListUsers(List<UserEntity> users);
    String addUserWTCP(UserEntity user);
    String addUserWTUN(UserEntity user);
    UserEntity UpdateUser(UserEntity user, Long id);
    List<UserEntity> getAllUsers();
    UserEntity getUserById(Long id);
    UserEntity getUserByUsername(String username);
    List<UserEntity>getUserSWT(String un);
    List<UserEntity>getUserByEmail(String un);

}
