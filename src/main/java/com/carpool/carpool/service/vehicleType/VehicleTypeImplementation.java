package com.carpool.carpool.service.vehicleType;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.dto.vehicleType.VehicleTypeResponseDTO;
import com.carpool.carpool.mappers.vehicleType.VehicleTypeMapper;
import com.carpool.carpool.model.vehicleType.VehicleType;
import com.carpool.carpool.repository.vehicleType.VehicleTypeRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleTypeImplementation implements IVehicleTypeService {

    
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleTypeMapper vehicleTypeMapper;


    /**
     * Metodo utilizado para obtener todos los tipos de vehículo disponibles en la base de datos.
     * @return Response<List<VehicleTypeResponseDTO>> devolviendo la lista de tipos de
     *
     */
    @Override
    @Transactional (readOnly = true)
    public Response<List<VehicleTypeResponseDTO>> getAllVehicleTypes() {
        List<VehicleType> vehicleTypes = vehicleTypeRepository.findAll();
        List<VehicleTypeResponseDTO> vehicleTypeResponseDTOs = vehicleTypes.stream()
                    .map(vehicleTypeMapper::convertVehicleTypeToVehicleTypeResponseDTO)
                    .collect(Collectors.toList());
        return ResponseUtils.buildOKResponse(List.of("Tipos de vehículo obtenidos con éxito."), vehicleTypeResponseDTOs);
    } 
   
}