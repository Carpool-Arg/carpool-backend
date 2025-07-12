package com.carpool.carpool.service.user;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.dto.user.UserUpdateRequestDTO;
import com.carpool.carpool.response.Response;

public interface IUserService {

    Response<Void> saveUser(UserRequestDTO userRequestDTO);
    Response<Void> updateUser(UserUpdateRequestDTO userUpdateRequestDTO, String email);
    Response<Void> validateUsername(String username);
    Response<Void> validateEmail(String email);
    Response<Void> validateDni (String dni);
}
