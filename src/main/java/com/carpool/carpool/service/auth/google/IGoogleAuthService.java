package com.carpool.carpool.service.auth.google;

import com.carpool.carpool.dto.user.google.GoogleAuthResponse;
import com.carpool.carpool.response.Response;

public interface IGoogleAuthService {

    Response<GoogleAuthResponse> authenticate(String idToken);
}
