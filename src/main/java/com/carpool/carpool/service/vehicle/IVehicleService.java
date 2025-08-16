package com.carpool.carpool.service.vehicle;

import java.util.List;

import com.carpool.carpool.dto.vehicle.VehicleOnlyResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleRequestDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleUpdateRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.response.Response;

public interface IVehicleService {
    
    /**
     * Metodo utilizado para almacenar un vehiculo en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes.
     * @param vehicleRequestDTO request con los datos del vehiculo a guardar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue creado
     * @throws ConflictException si ocurre un conflicto como dominio duplicado, acceso no autorizado o error lógico
     */
    Response<Void> saveVehicle(VehicleRequestDTO vehicleRequestDTO);
    
    /**
     * Metodo utilizado para actualizar un vehiculo en la base de datos.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param vehicleUpdateRequestDTO request con los datos del vehiculo a actualizar
     * @param id Id del vehiculo a actualizar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue actualizado
     * @throws ConflictException si no se encuentra el vehículo, no pertenece al chofer, está dado de baja o acceso no autorizado
     */
    Response<Void> updateVehicle(Long id, VehicleUpdateRequestDTO vehicleUpdateRequestDTO);

    /**
     * Metodo utilizado para eliminar un vehiculo de la base de datos.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param id Id del vehiculo a eliminar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue eliminado
     * @throws ConflictException si no se encuentra el vehículo, no pertenece al chofer, está dado de baja o acceso no autorizado
     */
    Response<Void> deleteVehicle(Long id);

    /**
     * Metodo utilizado para obtener un vehiculo por su id.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @return Response<VehicleResponseDTO> devolviendo el vehiculo encontrado
     * @throws ConflictException si no se encuentra el chofer autenticado o acceso no autorizado
     */
    Response<List<VehicleResponseDTO>> getVehiclesByAuthenticatedDriver();

    /**
     * Metodo utilizado para obtener un vehiculo por su id.
     * Se realizan controles para lanzar las excepciones correspondientes.
     * @param id Id del vehiculo a buscar
     * @return Response<VehicleOnlyResponseDTO> devolviendo el vehiculo encontrado
     * @throws ConflictException si no se encuentra el vehiculo, no pertenece al chofer, está dado de baja o acceso no autorizado
     */
    Response<VehicleOnlyResponseDTO> getVehicleById(Long id);
}
