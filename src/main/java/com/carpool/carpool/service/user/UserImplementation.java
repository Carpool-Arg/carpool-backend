package com.carpool.carpool.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.carpool.carpool.exception.ConflictException;
import lombok.RequiredArgsConstructor;
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
import com.carpool.carpool.utils.ResponseUtils;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserImplementation implements IUserService {

    private RoleRepository roleRepository;
    private UserMapper userMapper;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private static final String EXIST_USER = "Ya existe un usuario con el ";
    private static final String ROLE_USER = "ROLE_USER";

    /**
     * Metodo utilizado para almacenar un usuario en la base de datos. Se realizan controles para 
     * lanzar las excepciones correspondientes
     * @param userRequestDTO request con los datos del usuario a guardar 
     * @return Response<Void> devolviendo el mensaje si el usuario fue creado
     */
    @Override
    @Transactional
    public Response<Void> saveUser(UserRequestDTO userRequestDTO) {

        passwordsMatch(userRequestDTO.getPassword(), userRequestDTO.getConfirmPassword());
        existsByUsername(userRequestDTO.getUsername());
        existsByEmail(userRequestDTO.getEmail());
        existsByDni(userRequestDTO.getDni());

        Optional<Role> optionalRoleUser = roleRepository.findByName(ROLE_USER);
        List<Role> roles = new ArrayList<>();
        optionalRoleUser.ifPresent(roles::add);

        User user = userMapper.convertUserRequestDTOToUser(
            userRequestDTO, 
            passwordEncoder.encode(userRequestDTO.getPassword()), 
            roles);
        userRepository.save(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario creado") , null);
    }

    /**
     * Metodo para validar si un username ingresado por una persona se encuentra disponible o no.
     * @param username el nombre de usuario ingresado por la persona.
     */
    public Response<Void> validateUsername(String username){
        existsByUsername(username);
        return ResponseUtils.buildOKResponse(List.of("Nombre de usuario disponible") , null);
    }

    /**
     * Método para validar si un email ingresado por una persona se encuentra disponible o no.
     * @param email el email ingresado por la persona.
     */
    @Override
    public Response<Void> validateEmail(String email) {
        existsByEmail(email);
        return ResponseUtils.buildOKResponse(List.of("Email disponible") , null);
    }

    /**
     * Metodo para validar si un dni ingresado por una persona se encuentra disponible o no.
     * @param dni el dni ingresado por la persona.
     */
    @Override
    public Response<Void> validateDni(String dni) {
        existsByDni(dni);
        return ResponseUtils.buildOKResponse(List.of("DNI disponible") , null);
    }

    /**
     * Metodo para comprobar que las contraseña y la confirmacion de la misma coinciden
     * @param userPassword la contraseña del usuario
     * @param userConfirmPassword la confirmacion de la contraseña del usuario
     * @throws ConflictException si no coinciden
     */
    private void passwordsMatch(String userPassword, String userConfirmPassword){
        if(!userPassword.equals(userConfirmPassword)){
            throw new ConflictException("Las contraseñas ingresadas no coinciden");
        }
    }
    
    /**
     * Metodo para comprobar no exista otro usuario en la base de datos
     * con el mismo email que el ingresado 
     * @param email el email ingresado por el usuario
     * @throws ConflictException si hay un usuario registrado con este email
     */
    private void existsByEmail(String email){
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new IllegalArgumentException(EXIST_USER.concat("correo electrónico ingresado."));
        });
    }

    /**
     * Meotodo para comprobar que no exista otro usuario en la base de datos 
     * con el mismo nombre de usuario que el ingresado
     * @param username el nombre de usuario ingresado
     * @throws ConflictException si hay un usuario con este nombre de usuario
     */

    private void existsByUsername(String username){
        userRepository.findByUsername(username).ifPresent(user -> {
            throw new ConflictException(EXIST_USER.concat("nombre de usuario ingresado."));
        });
    } 

    /**
     * Metodo para comprobar que no exista otro usuario en la base de datos
     *  con el mismo dni que el ingresado
     * @param dni dni ingresado por el usuario
     * @throws ConflictException si hay un usuario registrado con este dni
     */
    private void existsByDni(String dni){
        userRepository.findByDni(dni).ifPresent(user -> { 
            throw new ConflictException(EXIST_USER.concat("DNI ingresado."));
        });
    }

}
