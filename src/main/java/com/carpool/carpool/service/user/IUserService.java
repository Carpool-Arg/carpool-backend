package com.carpool.carpool.service.user;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.response.Response;

public interface IUserService {
    Response<Void> saveUser(UserRequestDTO userRequestDTO);
    void validateUsername(String username);
    void validateEmail(String email);
    void validateDni (String dni);
}
