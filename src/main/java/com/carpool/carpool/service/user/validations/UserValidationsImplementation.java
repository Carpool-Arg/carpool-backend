package com.carpool.carpool.service.user.validations;

import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.user.UserBaseImplementation;
import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserValidationsImplementation {
    private final UserBaseImplementation userBaseImplementation;

    public Response<Void> validateUsername(String username) {
        userBaseImplementation.existsByUsername(username);
        return ResponseUtils.buildOKResponse(List.of("Nombre de usuario disponible"), null);
    }

    public Response<Void> validateEmail(String email) {
        userBaseImplementation.existsByEmail(email);
        return ResponseUtils.buildOKResponse(List.of("Email disponible"), null);
    }

    public Response<Void> validateDni(String dni) {
        userBaseImplementation.existsByDni(dni);
        return ResponseUtils.buildOKResponse(List.of("DNI disponible"), null);
    }
}
