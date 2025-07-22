package com.carpool.carpool.service.auth.recaptcha;

import com.carpool.carpool.dto.security.recaptcha.RecaptchaResponseDTO;

public interface IAuthRecaptchaService {
    /**
     * Valida un token de reCAPTCHA v3 contra la API de verificación de Google.
     * <p>
     * Este método realiza una solicitud HTTP POST al endpoint
     * {@code https://www.google.com/recaptcha/api/siteverify} con los siguientes
     * parámetros:
     * <ul>
     *   <li><strong>secret</strong>: clave secreta del backend (proporcionada por Google)</li>
     *   <li><strong>response</strong>: token generado en el frontend por {@code grecaptcha.execute()}</li>
     * </ul>
     * Google responde con un objeto JSON que contiene el resultado de la validación,
     * incluyendo campos como {@code success}, {@code score}, {@code action}, etc.
     * <p>
     * Este método transforma esa respuesta en una instancia de {@link RecaptchaResponseDTO}.
     *
     * @param recaptchaToken el token generado por el frontend (enviado desde el cliente)
     * @return una instancia de {@link RecaptchaResponseDTO} con los datos de validación devueltos por Google
     */
    RecaptchaResponseDTO validateToken(String recaptchaToken);
}
