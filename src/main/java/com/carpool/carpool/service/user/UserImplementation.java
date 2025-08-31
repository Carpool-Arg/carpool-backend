package com.carpool.carpool.service.user;


import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.dto.user.*;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.recovery.UserRecoveryImplementation;
import com.carpool.carpool.service.user.register.UserRegisterImplementation;
import com.carpool.carpool.service.user.update.UserUpdateImplementation;
import com.carpool.carpool.service.user.validations.UserValidationsImplementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserImplementation implements IUserService {

    private final UserRegisterImplementation userRegisterImplementation;
    private final UserUpdateImplementation userUpdateImplementation;
    private final UserRecoveryImplementation userRecoveryImplementation;
    private final UserValidationsImplementation userValidationsImplementation;

    //Metodos de registro de usuario
    @Override
    public Response<Void> saveUser(UserRequestDTO userRequestDTO) {
        return userRegisterImplementation.saveUser(userRequestDTO);
    }

    @Override
    public Response<Void> completeRegistration(UserUpdateRequestDTO userUpdateRequestDTO) {
        return userRegisterImplementation.completeRegistration(userUpdateRequestDTO);
    }

    @Override
    public Response<Void> resendActivateAccount(String email) {
        return userRegisterImplementation.resendActivateAccount(email);
    }

    //Metodos de actualización de perfil de un usuario 
    @Override
    public Response<UserResponseDTO> getAuthenticatedUser() {
        return userUpdateImplementation.getAuthenticatedUser();
    }

    @Override
    public Response<TokenResponseDTO> updateUserProfile(UserProfileUpdateRequestDTO userProfileUpdateRequestDTO, 
                                                       MultipartFile profileImage) {
        return userUpdateImplementation.updateUserProfile(userProfileUpdateRequestDTO, profileImage);
    }

    @Override
    public Response<TokenResponseDTO> updateUserEmail(EmailRequestDTO emailRequestDTO) {
        return userUpdateImplementation.updateUserEmail(emailRequestDTO);
    }

    @Override
    public Response<TokenResponseDTO> updateUserPassword(UserPasswordChangeRequestDTO passwordChangeRequestDTO) {
        return userUpdateImplementation.updateUserPassword(passwordChangeRequestDTO);
    }

    @Override
    public Response<Void> confirmEmailChange(String token) {
        return userUpdateImplementation.confirmEmailChange(token);
    }

    //Metodos de recuperación de cuenta
    @Override
    public Response<Void> sendPasswordChangeEmail(EmailRequestDTO emailRequestDTO) {
        return userRecoveryImplementation.sendPasswordChangeEmail(emailRequestDTO);
    }

    @Override
    public Response<Void> changePassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        return userRecoveryImplementation.changePassword(changePasswordRequestDTO);
    }

    //Metodos de validación
    @Override
    public Response<Void> validateUsername(String username) {
        return userValidationsImplementation.validateUsername(username);
    }

    @Override
    public Response<Void> validateEmail(String email) {
        return userValidationsImplementation.validateEmail(email);
    }

    @Override
    public Response<Void> validateDni(String dni) {
        return userValidationsImplementation.validateDni(dni);
    }

}
