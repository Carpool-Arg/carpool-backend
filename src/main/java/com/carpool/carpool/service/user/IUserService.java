package com.carpool.carpool.service.user;

import com.carpool.carpool.dto.user.UserRequestDTO;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.response.Response;

public interface IUserService {
    Response<User> saveUser(UserRequestDTO userRequestDTO);
}
