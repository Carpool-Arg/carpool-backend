package com.carpool.carpool.service.vehicle;

import java.util.List;

import com.carpool.carpool.dto.vehicle.VehicleRequestDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleUpdateRequestDTO;
import com.carpool.carpool.response.Response;

public interface IVehicleService {
    
    /**
     * Metodo utilizado para almacenar un vehiculo en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes.
     * @param vehicleRequestDTO request con los datos del vehiculo a guardar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue creado
     */
    Response<Void> saveVehicle(VehicleRequestDTO vehicleRequestDTO);
    
    /**
     * Metodo utilizado para actualizar un vehiculo en la base de datos.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param vehicleUpdateRequestDTO request con los datos del vehiculo a actualizar
     * @param id Id del vehiculo a actualizar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue actualizado
     */
    Response<Void> updateVehicle(Long id, VehicleUpdateRequestDTO vehicleUpdateRequestDTO);

    /**
     * Metodo utilizado para eliminar un vehiculo de la base de datos.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param id Id del vehiculo a eliminar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue eliminado
     */
    Response<Void> deleteVehicle(Long id);

    /**
     * Metodo utilizado para obtener un vehiculo por su id.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @return Response<VehicleResponseDTO> devolviendo el vehiculo encontrado
     */
    Response<List<VehicleResponseDTO>> getVehiclesByAuthenticatedDriver();
}
