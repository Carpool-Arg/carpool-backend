package com.carpool.carpool.service.user.validations;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.response.Response;

public interface IUserRegisterService {

    /**
     * Metodo utilizado para almacenar un usuario en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes. Tambien crea un registro para activar la cuenta del usuario.
     * @param userRequestDTO request con los datos del usuario a guardar
     * @return Response<Void> devolviendo el mensaje si el usuario fue creado
     */
    Response<Void> saveUser(UserRequestDTO userRequestDTO);

    /**
     * Metodo utilizado para actualizar un usuario con registro parcial en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes. Tambien crea un registro para activar la cuenta del usuario.
     * @param {@link UserUpdateRequestDTO} request con los datos del usuario a guardar
     * @return {@link Response<Void>} devolviendo el mensaje si el usuario fue creado
     */
    Response<Void> updateUser(UserUpdateRequestDTO userUpdateRequestDTO);

    /**
     * Metodo encargado de enviar nuevamente un correo electrónico para que el usuario pueda activar su cuenta. En caso de que el usuario no exista en la
     * base de datos se retornará igualmente un estado 200, ya que no deseamos brindar información privada de nuestros usuarios.
     * @param email del tipo {@link String}
     * @return {@link Response<Void>} devolviendo el mensaje si se envió el correo
     */
    Response<Void> resendActivateAccount(String email);
}
