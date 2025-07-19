package com.carpool.carpool.service.auth.google;

import com.carpool.carpool.dto.google.GoogleAuthResponse;
import com.carpool.carpool.response.Response;

public interface IAuthGoogleService {

    /**
     * Realiza el login con Google: si el usuario existe y está activo, se autentica, si no existe, se registra parcialmente con estado {@code PENDING_PROFILE}. Este metodo
     * retorna el {@code accessToken} y {@code refreshToken} cuando el usuario se encuentra en estado {@code ACTIVE} o {@code PENDING_PROFILE} para que pueda acceder a funcionalidades del sistema.
     * Ademas, delega la logica para el envio de correo electronico para que le usuario pueda activar su cuenta.
     * @param idToken Token id proporcionado por Google.
     * @return Objeto {@link Response} que contiene el {@link GoogleAuthResponse}
     */
    Response<GoogleAuthResponse> authenticate(String idToken);
}
