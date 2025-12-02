package com.carpool.carpool.service.user;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.dto.user.UserPasswordChangeRequestDTO;
import com.carpool.carpool.dto.user.UserProfileUpdateRequestDTO;

import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.dto.user.EmailRequestDTO;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserResponseDTO;
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
    Response<Void> completeRegistration(UserUpdateRequestDTO userUpdateRequestDTO);

    /**
     * Metodo encargado de enviar nuevamente un correo electrónico para que el usuario pueda activar su cuenta. En caso de que el usuario no exista en la
     * base de datos se retornará igualmente un estado 200, ya que no deseamos brindar información privada de nuestros usuarios.
     * @param email del tipo {@link String}
     * @return {@link Response<Void>} devolviendo el mensaje si se envió el correo
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
     * Metodo apra validar si un numero de telefono ingresado por una persona se encuentra disponible o no. 
     * @param phone el telefono ingresado
     * @return
     */
    Response<Void> validatePhone(String phone);

    /**
     * Metodo para validar que un usuario tiene mas de 18 años
     * @param birthDate la fecha de nacimiento del usuario
     * @return
     */
    Response<Void> validateBirthDate(LocalDate birthDate);

    /**
     * Metodo para actualizar el perfil del usuario.
     * Este método recibe el nombre de usuario y un DTO con los datos a actualizar.
     * Se espera que el DTO contenga los campos que se pueden modificar en el perfil del usuario,
     * como nombre, apellido, género, teléfono e imagen de perfil.
     * @param userProfileUpdateRequestDTO
     */
    Response<TokenResponseDTO> updateUserProfile(UserProfileUpdateRequestDTO userProfileUpdateRequestDTO, MultipartFile profileImage);

    /**
     * Metodo para actualizar el email del usuario.
     * Este método recibe un DTO con el email actual y el nuevo email.
     * Se espera que el DTO contenga los campos necesarios para validar el cambio de email,
     * como el email actual, el nuevo email y la contraseña del usuario.
     * @param emailRequestDTO
     * @return
     */
    Response<TokenResponseDTO> updateUserEmail(EmailRequestDTO emailRequestDTO);

    /**
     * Metodo para actualizar la contraseña del usuario.
     * Este método recibe un DTO con la contraseña actual, la nueva contraseña y la confirmación
     * de la nueva contraseña.
     * Se espera que el DTO contenga los campos necesarios para validar el cambio de contraseña,
     * @param passwordChangeRequestDTO
     * @return
     */
    Response<TokenResponseDTO> updateUserPassword(UserPasswordChangeRequestDTO passwordChangeRequestDTO);

    /**
     * Confirma el cambio de email usando un token de verificación.
     * @param token Token de verificación enviado al nuevo email.
     * @return Response<Void> indicando el resultado de la operación.
     */
    Response<Void> confirmEmailChange(String token);

    /**
     * Genera un DTO de respuesta con los datos del usuario.
     * @return Response<Void> con los datos del usuario.
     */
    Response<UserResponseDTO> getAuthenticatedUser ();

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
