package com.carpool.carpool.mappers.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;

@Component
public class UserMapper {
    public User convertUserRequestDTOToUser(UserRequestDTO userRequestDTO, String encritpedPassword, List<Role> roles){
        return User.builder()
            .name(userRequestDTO.getName())
            .lastname(userRequestDTO.getLastname())
            .username(userRequestDTO.getUsername())
            .password(encritpedPassword)
            .dni(userRequestDTO.getDni())
            .email(userRequestDTO.getEmail())
            .phone(userRequestDTO.getPhone())
            .roles(roles)
            .build();
    } 
}
