package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.dto.UserDto;
import com.example.demo.Entitys.User;

public interface UserService {
    void saveUser(UserDto userDto);


    List<UserDto> findAllUsers();
    Optional<User> findByEmail(String email);
    Optional<UserDto> findUserDtoByEmail(String email); // Nuevo método
 
    
    // Método para convertir User a UserDto
    UserDto convertToDto(User user);


}

