package com.carpool.carpool.service.user;

import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;
import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.response.Response;

public interface IUserService {
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
     * Metodo encargado de validar si el token se encuentra en la base de datos y si cumple condiciones para proceder a la
     * activacion de la cuenta del usuario y tambien a la caducación del token
     * @param token del tipo {@link String}
     * @return
     */
    Response<Void> activateAccount(String token);

    /**
     * Metodo encargado de enviar nuevamente un correo electrónico para que el usuario pueda activar su cuenta. En caso de que el usuario no exista en la
     * base de datos se retornará igualmente un estado 200, ya que no deseamos brindar información privada de nuestros usuarios.
     * @param email del tipo {@link String}
     */
    Response<Void> resendActivateAccount(String email);

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

    /**
     * Metodo para el envio de correo electronico de solicitud de cambio de contraseña.
     * @param emailRequestDTO un email ingresado por la persona con las validaciones de formato
     */
    Response<Void> sendPasswordChangeEmail(EmailRequestDTO emailRequestDTO);

    /**
     * Metodo para realizar el cambio de la contraseña del usuario
     * @param changePasswordRequestDTO contiene la contraseña nueva, la confirmacion de la misma y el token 
     * del usuario para el cambio de contraseña
     */
    Response<Void> changePassword(ChangePasswordRequestDTO changePasswordRequestDTO);
}
