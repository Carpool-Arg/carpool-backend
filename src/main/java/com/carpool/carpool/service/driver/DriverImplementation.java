package com.carpool.carpool.service.driver;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.mappers.driver.DriverMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.utils.ResponseUtils;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverImplementation implements IDriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private UserRepository userRepository;

    //Para asignar roles a los choferes, se inyecta el RoleRepository
    @Autowired
    private RoleRepository roleRepository; 

    private static final String ROLE_DRIVER = "ROLE_DRIVER";
    private static final String EXIST_DRIVER_PROFILE = "Ya existe un perfil de chofer para este usuario.";
    private static final int MIN_DRIVER_AGE = 18;


    /**
     * Metodo utilizado para almacenar un chofer en la base de datos. Se realizan controles para
     * lanzar las excepciones correspondientes.
     * @param driverRequestDTO request con los datos del chofer a guardar
     * @return Response<Void> devolviendo el mensaje si el chofer fue creado
     * 
     */
    @Override
    @Transactional
    public Response<Void> saveDriver(DriverRequestDTO driverRequestDTO) {
        
        checkDriverAge(driverRequestDTO.getBirthDate());
        checkIfDriverProfileExists(); 

        //Obtener el usuario autenticado. 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        //Buscar el usuario por su nombre de usuario.
        User user = userRepository.findByUsername(username)
                .orElseThrow( () -> new ConflictException("Usuario no encontrado.")); 
        
        
        Driver driver = driverMapper.convertDriverRequestDTOToDriver(driverRequestDTO, user);
        assignDriverRoleToUser(user); 
        driverRepository.save(driver);
        updateSecurityContext(user); 

        return ResponseUtils.buildOKResponse(List.of("El perfil de chofer ha sido creado correctamente."), null);
    }
 
    /**
     * Metodo utilizado para verificar la edad del chofer.
     * Se lanza una excepcion si la fecha de nacimiento es en el futuro o si el chofer es menor de edad.
     * @param birthDate fecha de nacimiento del chofer
     * @return void 
     */
    private void checkDriverAge(LocalDate birthDate) {
        LocalDate currentDate = LocalDate.now();
        if (birthDate.isAfter(currentDate)) {
            throw new ConflictException("La fecha de nacimiento no puede ser en el futuro.");
        }
        int age = Period.between(birthDate, currentDate).getYears();
        if (age < MIN_DRIVER_AGE) { 
            throw new ConflictException("El chofer debe tener al menos " + MIN_DRIVER_AGE + " años de edad.");
        }
    }

    /**
     * Metodo utilizado para verificar si el usuario ya tiene un perfil de chofer.
     * Si ya existe un perfil de chofer, se lanza una excepcion.
     * @param userId id del usuario
     * @return void
     */
    private void checkIfDriverProfileExists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow( () -> new ConflictException("Usuario no encontrado."));

        Optional<Driver> existingDriver = driverRepository.findByUserId(user.getId());
        if (existingDriver.isPresent()) {
            throw new ConflictException(EXIST_DRIVER_PROFILE);
        }
    }

    /**
     * Metodo utilizado para asignar el rol de chofer al usuario.
     * Se verifica si el usuario ya tiene el rol de chofer,
     * @param user
     * @return void
     */
    private void assignDriverRoleToUser(User user) {
        // Usamos la constante ROLE_DRIVER definida arriba
        Role driverRole = roleRepository.findByName(ROLE_DRIVER) // <-- ¡Aquí se usa la constante!
             .orElseThrow( () -> new ConflictException("Rol '" + ROLE_DRIVER + "' no encontrado.")); 
        
        List<Role> userRoles = new ArrayList<>(user.getRoles()); 
        if (!userRoles.contains(driverRole)) { 
            userRoles.add(driverRole); 
            user.setRoles(userRoles); 
            userRepository.save(user); 
        }
    }


    /**
     * Metodo utilizado para actualizar el SecurityContext del usuario
     * con los nuevos detalles del chofer.
     * @param user
     * @return void
     * 
     */
    private void updateSecurityContext(User user) {
        CustomUserDetails updatedUserDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(
                        updatedUserDetails,
                        null, 
                        updatedUserDetails.getAuthorities() 
                );

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }
}
