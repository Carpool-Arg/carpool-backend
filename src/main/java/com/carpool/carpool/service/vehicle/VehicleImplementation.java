package com.carpool.carpool.service.vehicle;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.dto.vehicle.VehicleOnlyResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleRequestDTO;
import com.carpool.carpool.dto.vehicle.VehicleResponseDTO;
import com.carpool.carpool.dto.vehicle.VehicleUpdateRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.mappers.vehicle.VehicleMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.model.vehicle.Vehicle;
import com.carpool.carpool.model.vehicle.type.VehicleType;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.trip.TripRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.vehicle.VehicleRepository;
import com.carpool.carpool.repository.vehicle.type.VehicleTypeRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleImplementation implements IVehicleService {

    
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final DriverRepository driverRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    /**
     * Metodo utilizado para almacenar un vehiculo en la base de datos.
     * Se obtienen los datos del chofer del contexto de seguridad.
     *
     * @param vehicleRequestDTO request con los datos del vehiculo a guardar.
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue creado.
     */
    @Override
    @Transactional
    public Response<Void> saveVehicle(VehicleRequestDTO vehicleRequestDTO) { 
        validateUniqueDomain(vehicleRequestDTO.getDomain());

        //Llamada al metodo que obtiene el chofer autenticado
        Driver driver = getAuthenticatedDriver(); 

        VehicleType vehicleType = getVehicleTypeById(vehicleRequestDTO.getVehicleTypeId());

        Vehicle vehicle = vehicleMapper.convertVehicleRequestDTOToVehicle(vehicleRequestDTO, driver, vehicleType);

        normalizeVehicle(vehicle);
        vehicleRepository.save(vehicle);

        return ResponseUtils.buildOKResponse(List.of("Vehiculo Creado"), null);
    }

    /**
     * Metodo utilizado para actualizar un vehiculo en la base de datos.
     * @param id Id del vehiculo a actualizar
     * @param vehicleUpdateRequestDTO request con los datos del vehiculo a actualizar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue actualizado
     */
    @Override
    public Response<Void> updateVehicle(Long id, VehicleUpdateRequestDTO vehicleUpdateRequestDTO) {
        
        Driver authenticatedDriver = getAuthenticatedDriver();
        
        Vehicle existingVehicle = vehicleRepository.findByIdAndDriver(id, authenticatedDriver)
                .orElseThrow(() -> new ConflictException("Vehiculo no encontrado o no pertenece al chofer.")); 
        
        if (!existingVehicle.isEnabled()) { 
            throw new ConflictException("No se puede editar un vehículo que ha sido dado de baja.");
        }

        vehicleHasPendingOrInProgressTrip(existingVehicle.getId(), true);

        VehicleType vehicleType = getVehicleTypeById(vehicleUpdateRequestDTO.getVehicleTypeId());

        vehicleMapper.convertVehicleUpdateRequestDTOToVehicle(vehicleUpdateRequestDTO, existingVehicle, vehicleType);
        normalizeVehicle(existingVehicle);

        vehicleRepository.save(existingVehicle);
        return ResponseUtils.buildOKResponse(List.of("Vehiculo Actualizado"), null);
    }

    /**
     * Metodo utilizado para eliminar un vehiculo de la base de datos.
     * 
     * @param id Id del vehiculo a eliminar
     * @return Response<Void> devolviendo el mensaje si el vehiculo fue eliminado
     */
    @Override
    public Response<Void> deleteVehicle(Long id) {

        Driver authenticatedDriver = getAuthenticatedDriver();

        Vehicle vehicleToDelete = vehicleRepository.findByIdAndDriver(id, authenticatedDriver)
                .orElseThrow(() -> new ConflictException("Vehículo no encontrado o no pertenece al chofer autenticado."));

       
        if (!vehicleToDelete.isEnabled()) { 
            throw new ConflictException("El vehículo con ID " + id + " ya se encuentra dado de baja.");
        }

        vehicleHasPendingOrInProgressTrip(id, false);

        vehicleToDelete.setDeletedAt(LocalDateTime.now());
        vehicleToDelete.setDeleted_by(authenticatedDriver.getUser().getId());
        vehicleRepository.save(vehicleToDelete);

        return ResponseUtils.buildOKResponse(List.of("Vehículo dado de baja correctamente."), null);
    }

    /**
     * Metodo utilizado para obtener los vehiculos del chofer autenticado.
     * 
     * @param id Id del vehiculo a buscar
     * @return Response<List<VehicleResponseDTO>> devolviendo la lista de vehiculos encontrados
     */
    @Override
    @Transactional(readOnly = true)
    public Response<List<VehicleResponseDTO>> getVehiclesByAuthenticatedDriver() {
        Driver authenticatedDriver = getAuthenticatedDriver();

        List<Vehicle> activeVehicles = vehicleRepository.findByDriverAndDeletedAtIsNull(authenticatedDriver);

        if (activeVehicles.isEmpty()) {
            return ResponseUtils.buildOKResponse(
                List.of("No tienes vehículos registrados. ¡Haz click aquí para agregar uno nuevo!"),
                List.of());
        }

        List<VehicleResponseDTO> vehicleResponseDTOs = vehicleMapper.convertVehicleListToVehicleResponseDTOList(activeVehicles);
        return ResponseUtils.buildOKResponse(List.of("Listado de vehículos activos."),vehicleResponseDTOs);
    }

    /**
     * Metodo utilizado para obtener un vehiculo por su id.
     * @param id Id del vehiculo a buscar
     * @return Response<VehicleOnlyResponseDTO> devolviendo el vehiculo encontrado
     */
    @Override
    public Response<VehicleOnlyResponseDTO> getVehicleById(Long id) {
        Driver authenticatedDriver = getAuthenticatedDriver();
        Vehicle vehicle = vehicleRepository.findByIdAndDriver(id, authenticatedDriver)
                .orElseThrow(() -> new ConflictException("Vehículo no encontrado o no pertenece al chofer autenticado."));
        
        if(!vehicle.isEnabled()) {
            throw new ConflictException("El vehículo con ID " + id + " está dado de baja.");
        }

        VehicleOnlyResponseDTO vehicleOnlyResponseDTO = vehicleMapper.convertVehicleToVehicleOnlyResponseDTO(vehicle);
        return ResponseUtils.buildOKResponse(List.of("Vehículo encontrado."), vehicleOnlyResponseDTO);
    }

    /**
     * Método auxiliar privado para obtener el Driver autenticado del contexto de seguridad.
     * Encapsula la lógica común para evitar repeticiones.
     *
     * @return La entidad Driver del usuario autenticado.
     * @throws ConflictException Si el usuario no está autenticado o no tiene un perfil de chofer.
     */
    private Driver getAuthenticatedDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."));

        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ConflictException("No se encontró el perfil de chofer para el usuario autenticado."));
    }

    /**
     * Valida que el dominio del vehiculo sea unico entre los vehiculos activos.
     * @param domain El dominio del vehiculo a validar.
     * @throws ConflictException Si ya existe un vehiculo activo con el mismo dominio.
     */
    private void validateUniqueDomain(String domain) {
        vehicleRepository.findByDomainIgnoreCaseAndDeletedAtIsNull(domain)
            .ifPresent(v -> {
                throw new ConflictException("Ya existe un vehículo activo con la patente: " + domain.toUpperCase());
            });
    }

    /**
     * Busca un VehicleType por su ID.
     * @param vehicleTypeId El ID del tipo de vehículo.
     * @return La entidad VehicleType encontrada.
     * @throws ConflictException Si el tipo de vehículo no es encontrado.
     */
    private VehicleType getVehicleTypeById(Long vehicleTypeId) {
        return vehicleTypeRepository.findById(vehicleTypeId)
                .orElseThrow(() -> new ConflictException("Tipo de vehículo no encontrado con ID: " + vehicleTypeId));
    }

    /**
     * Normaliza los campos del vehiculo a mayusculas y elimina espacios en blanco.
     * @param vehicle El vehiculo a normalizar.
     */
    private void normalizeVehicle(Vehicle vehicle) {
        vehicle.setDomain(vehicle.getDomain().toUpperCase().trim().replaceAll("\\s+", " "));
        vehicle.setBrand(vehicle.getBrand().toUpperCase().trim().replaceAll("\\s+", " "));
        vehicle.setModel(vehicle.getModel().toUpperCase().trim().replaceAll("\\s+", " "));
        vehicle.setColor(vehicle.getColor().toUpperCase().trim().replaceAll("\\s+", " "));
    }

    /**
     * Metodo para determinar si un vehiculo tiene un viaje pendiente o esta en un viaje en curso
     */

    private void vehicleHasPendingOrInProgressTrip(Long vehicleId, boolean isEdit){
        if (tripRepository.vehicleHasInProgressTrip(vehicleId)){
            throw new ConflictException(isEdit ? "No se pueden modificar los datos del vehículo porque tiene un viaje en progreso." : "No se puede dar de baja el vehículo porque tiene un viaje en progreso.");
        };

        if(tripRepository.vehicleHasPendingTrip(vehicleId)){
            throw new ConflictException(isEdit ? "No se pueden modificar los datos del vehículo porque tiene un viaje programado." : "No se puede dar de baja el vehículo porque tiene un viaje programado.");
        };
    } 
}