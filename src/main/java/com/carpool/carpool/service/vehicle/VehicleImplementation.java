package com.carpool.carpool.service.vehicle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        VehicleType vehicleType = getVehicleTypeById(vehicleRequestDTO.getVehicleType_Id());

        Vehicle vehicle = vehicleMapper.convertVehicleRequestDTOToVehicle(vehicleRequestDTO, driver, vehicleType);

        normalizeVehicle(vehicle);
        vehicleRepository.save(vehicle);

        return ResponseUtils.buildOKResponse(List.of("Vehiculo Creado"), null);
    }

    @Override
    public Response<Void> updateVehicle(Long id, VehicleUpdateRequestDTO vehicleUpdateRequestDTO) {
        
        Driver authenticatedDriver = getAuthenticatedDriver();
        
        Vehicle existingVehicle = vehicleRepository.findByIdAndDriver(id, authenticatedDriver)
                .orElseThrow(() -> new ConflictException("Vehiculo no encontrado o no pertenece al chofer.")); 
        
        if (!existingVehicle.isEnabled()) { 
            throw new ConflictException("No se puede editar un vehículo que ha sido dado de baja.");
        }

        vehicleMapper.convertVehicleUpdateRequestDTOToVehicle(vehicleUpdateRequestDTO, existingVehicle);
        normalizeVehicle(existingVehicle);

        vehicleRepository.save(existingVehicle);
        return ResponseUtils.buildOKResponse(List.of("Vehiculo Actualizado"), null);
    }

    @Override
    public Response<Void> deleteVehicle(Long id) {

        Driver authenticatedDriver = getAuthenticatedDriver();

        Vehicle vehicleToDelete = vehicleRepository.findByIdAndDriver(id, authenticatedDriver)
                .orElseThrow(() -> new ConflictException("Vehículo no encontrado o no pertenece al chofer autenticado."));

       
        if (!vehicleToDelete.isEnabled()) { 
            throw new ConflictException("El vehículo con ID " + id + " ya se encuentra dado de baja.");
        }

        vehicleToDelete.setDeletedAt(LocalDateTime.now());
        vehicleRepository.save(vehicleToDelete);

        return ResponseUtils.buildOKResponse(List.of("Vehículo dado de baja correctamente."), null);
    }

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
     * Valida que el dominio del vehiculo sea unico.
     * @param domain El dominio del vehiculo a validar.
     * @throws ConflictException Si ya existe un vehiculo con el mismo dominio.
     */
    private void validateUniqueDomain(String domain) {
        Optional<Vehicle> existingVehicle = vehicleRepository.findByDomain(domain);
        if (existingVehicle.isPresent()) {
            throw new ConflictException("La patente '" + domain + "' ya se encuentra registrada.");
        }
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
        vehicle.setDomain(vehicle.getDomain().toUpperCase().trim());
        vehicle.setBrand(vehicle.getBrand().toUpperCase().trim());
        vehicle.setModel(vehicle.getModel().toUpperCase().trim());
        vehicle.setColor(vehicle.getColor().toUpperCase().trim());
    }

    
}