package com.carpool.carpool.service.auth.google;

import com.carpool.carpool.dto.google.GoogleAuthResponse;
import com.carpool.carpool.response.Response;

public interface IAuthGoogleService {

    Response<GoogleAuthResponse> authenticate(String idToken);
}
