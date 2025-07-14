package com.carpool.carpool.service.auth.google;

import com.carpool.carpool.dto.google.GoogleAuthResponse;
import com.carpool.carpool.response.Response;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public interface IAuthGoogleService {

    Response<GoogleAuthResponse> authenticate(String idToken);
}
