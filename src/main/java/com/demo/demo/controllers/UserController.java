package com.demo.demo.controllers;

import com.demo.demo.entities.RoleName;
import com.demo.demo.entities.UserEntity;
import com.demo.demo.services.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    UserInterface userInterface;

    @GetMapping("afficher")
    public String user(){
        return "Hey nour";
    }
    @GetMapping("users")
    public ResponseEntity<Map<String,Object>> getUser(){
        Map<String,Object> response= new HashMap<>();
        response.put("status","ok");
        List<String> users= new ArrayList<>();
        users.add("john");
        users.add("john");
        users.add("mary");
        users.add("jane");
        Set<String> users1= new HashSet<>();
        users1.add("john");
        users1.add("john");
        users1.add("mary");
        users1.add("jane");
        response.put("data",users);
        response.put("data2",users1);
        return ResponseEntity.ok(response);
    }
    @PostMapping("add")
    public UserEntity addUser(@RequestBody UserEntity user){
        return userInterface.adduser(user);
    }
    @DeleteMapping("delete/{id}")
    public void deleteUser(@PathVariable Long id){
        userInterface.deletedUser(id);
    }
    @DeleteMapping("delete")
    public void deleteUserz(@RequestParam("a") Long id){
        userInterface.deletedUser(id);
    }
     @PostMapping("saveall")
     public List<UserEntity> addListUsers(@RequestBody List<UserEntity> users){
        return userInterface.AddListUsers(users);
     }
     @PostMapping("addwithconfpassword")
    public String addUserWithConfPassword(@RequestBody UserEntity user){
        return userInterface.addUserWTCP(user);
     }
      @PostMapping("addWTUN")
    public String addUserWTUN(@RequestBody UserEntity user){
        return userInterface.addUserWTUN(user);
      }
      @PutMapping("updateuser/{id}")
    public UserEntity  updateuser(@PathVariable Long id, @RequestBody UserEntity user){
        return userInterface.UpdateUser(user,id);
      }
      @GetMapping("all")
    public List<UserEntity> getAllUser(){
        return userInterface.getAllUsers();
      }
      @GetMapping("findbyusername/{abc}")
    public UserEntity getUserByUsername(@PathVariable("abc") String u){
        return userInterface.getUserByUsername(u);
      }
      @GetMapping("findbyid/{id}")
    public UserEntity getUserById(@PathVariable Long id){
        return userInterface.getUserById(id);
      }
      @GetMapping("getuserswt/{cle}")
    public List<UserEntity> getUserSW(@PathVariable String cle){
        return userInterface.getUserSWT(cle);

      }
    @GetMapping("getuserbyemaildomaine")
    public List<UserEntity> getUserbyemaildom(@RequestParam String cle){
        return userInterface.getUserByEmail(cle);

    }
   }
