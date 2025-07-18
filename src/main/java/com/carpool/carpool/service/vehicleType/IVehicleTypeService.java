package com.carpool.carpool.service.vehicleType;


import java.util.List;

import com.carpool.carpool.dto.vehicleType.VehicleTypeResponseDTO;
import com.carpool.carpool.response.Response;

public interface IVehicleTypeService {
    Response<List<VehicleTypeResponseDTO>> getAllVehicleTypes();

}
