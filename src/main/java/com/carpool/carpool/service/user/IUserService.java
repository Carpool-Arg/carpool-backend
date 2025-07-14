package com.carpool.carpool.service.user;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.response.Response;

public interface IUserService {
    /**
     * Metodo utilizado para almacenar un usuario en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes
     * @param userRequestDTO request con los datos del usuario a guardar
     * @return Response<Void> devolviendo el mensaje si el usuario fue creado
     */
    Response<Void> saveUser(UserRequestDTO userRequestDTO);

    /**
     * Metodo utilizado para actualizar un usuario con registro parcial en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes
     * @param {@link UserUpdateRequestDTO} request con los datos del usuario a guardar
     * @return {@link Response<Void>} devolviendo el mensaje si el usuario fue creado
     */
    Response<Void> updateUser(UserUpdateRequestDTO userUpdateRequestDTO, String email);

    /**
     * Metodo para validar si un username ingresado por una persona se encuentra disponible o no.
     * @param username el nombre de usuario ingresado por la persona.
     */
    Response<Void> validateUsername(String username);

    /**
     * Método para validar si un email ingresado por una persona se encuentra disponible o no.
     * @param email el email ingresado por la persona.
     */
    Response<Void> validateEmail(String email);

    /**
     * Metodo para validar si un dni ingresado por una persona se encuentra disponible o no.
     * @param dni el dni ingresado por la persona.
     */
    Response<Void> validateDni (String dni);
}
