package com.carpool.carpool.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDebtResponseDTO {
    private Long reservationId;
    private Double total;
    private Boolean debtUser;
    private Boolean expired;
}
