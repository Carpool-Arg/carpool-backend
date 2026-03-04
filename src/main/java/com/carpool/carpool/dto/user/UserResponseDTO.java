package com.carpool.carpool.dto.user;


import com.carpool.carpool.enums.user.UserGenderEnum;
import com.carpool.carpool.enums.user.UserStateEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String name;
    private String lastname;
    private String username;
    private String email;
    private String dni;
    private String phone;
    private String birthDate;
    private UserGenderEnum gender;
    private UserStateEnum status;
    private double passengerRating;
    private double driverRating;

}
