package com.carpool.carpool.mappers.user;

import java.util.List;

import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.enums.user.UserStateEnum;
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
            .status(UserStateEnum.PENDING_VERIFICATION)
            .roles(roles)
            .build();
    }

    public User convertUserUpdateRequestDTOToUser(User user, UserUpdateRequestDTO userUpdateRequestDTO, String encritpedPassword, List<Role> roles){
        return user.toBuilder()
                .lastname(userUpdateRequestDTO.getLastname())
                .username(userUpdateRequestDTO.getUsername())
                .password(encritpedPassword)
                .dni(userUpdateRequestDTO.getDni())
                .phone(userUpdateRequestDTO.getPhone())
                .status(UserStateEnum.PENDING_VERIFICATION)
                .roles(roles)
                .build();
    }
}
