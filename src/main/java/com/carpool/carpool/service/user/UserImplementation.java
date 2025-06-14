package com.carpool.carpool.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.mappers.user.UserMapper;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.response.ResponseStateEnum;

import jakarta.transaction.Transactional;

@Service
public class UserImplementation implements IUserService {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public Response<User> saveUser(UserRequestDTO userRequestDTO) {
        if(userRequestDTO.getPassword().equals(userRequestDTO.getConfirmPassword())){

            Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");

            List<Role> roles = new ArrayList<>();
            
            optionalRoleUser.ifPresent(roles::add);
            
            User user = userMapper.convertUserRequestDTOToUser(
                userRequestDTO, 
                passwordEncoder.encode(userRequestDTO.getPassword()), 
                roles);
            userRepository.save(user);
            
            Response<User> response = new Response<>(List.of("Usuario creado"), ResponseStateEnum.OK);
            return response;
        }else{
            Response<User> response = new Response<>(List.of("Usuario no creado"), ResponseStateEnum.ERROR);
            return response;
        }   

    }

}
