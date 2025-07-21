package com.carpool.carpool.service.vehicle.type;


import java.util.List;

import com.carpool.carpool.dto.vehicle.type.VehicleTypeResponseDTO;
import com.carpool.carpool.response.Response;

public interface IVehicleTypeService {
    Response<List<VehicleTypeResponseDTO>> getAllVehicleTypes();

}
