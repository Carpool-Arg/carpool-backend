package com.carpool.carpool.service.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.carpool.carpool.model.licenseClass.LicenseClass;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.repository.licenseClass.LicenseClassRepository;
import com.carpool.carpool.repository.media.MediaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.carpool.carpool.dto.driver.DriverLicenseVerifyRequestDTO;
import com.carpool.carpool.dto.driver.DriverPendingPageResponseDTO;
import com.carpool.carpool.dto.driver.DriverPendingResponseDTO;
import com.carpool.carpool.dto.driver.DriverRequestDTO;
import com.carpool.carpool.dto.driver.DriverResponseDTO;
import com.carpool.carpool.dto.security.token.TokenResponseDTO;
import com.carpool.carpool.enums.licenseStatus.LicenseStatusEnum;
import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.enums.notificationEvent.NotificationEventEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.mappers.driver.DriverMapper;
import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.role.Role;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.city.CityRepository;
import com.carpool.carpool.repository.driver.DriverRepository;
import com.carpool.carpool.repository.role.RoleRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.security.model.CustomUserDetails;
import com.carpool.carpool.service.media.IMediaService;
import com.carpool.carpool.service.notification.INotificationService;
import com.carpool.carpool.service.r2.IR2StorageService;
import com.carpool.carpool.utils.ResponseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.carpool.carpool.security.utils.JwtUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverImplementation implements IDriverService {


    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final LicenseClassRepository licenseClassRepository;
    private final IR2StorageService r2StorageService;
    private final MediaRepository mediaRepository;
    private final IMediaService mediaService; 
    private final INotificationService notificationService;

    //Para asignar roles a los choferes, se inyecta el RoleRepository
    private final RoleRepository roleRepository;

    private static final String ROLE_DRIVER = "ROLE_DRIVER";
    private static final String EXIST_DRIVER_PROFILE = "Ya existe un perfil de chofer para este usuario.";

    // Constantes para los claims
    public final static String AUTHORITIES_CLAIM = "authorities";
    private final static String USERNAME_CLAIM = "username";

    @Override
    @Transactional
    public Response<TokenResponseDTO> saveDriver(DriverRequestDTO driverRequestDTO, MultipartFile frontPhoto, MultipartFile backPhoto) {

        checkIfDriverProfileExists();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ConflictException("Usuario no encontrado."));

        City city = cityRepository.findById(driverRequestDTO.getCityId())
                .orElseThrow(() -> new ConflictException("La ciudad no existe."));

        LicenseClass licenseClass = licenseClassRepository.findById(driverRequestDTO.getLicenseClassId())
                .orElseThrow(() -> new ConflictException("La clase de licencia" + driverRequestDTO.getLicenseClassId() +" no existe."));

        Driver driver = driverMapper.convertDriverRequestDTOToDriver(driverRequestDTO, user, city, licenseClass);

        if (driver.getRating() == null) {
            driver.setRating(5.0); 
        }

        //Se setea por defecto el valor de PEndding dentro de la aprobación. 
        driver.setLicenseStatus(LicenseStatusEnum.PENDING);

        assignDriverRoleToUser(user);
        normalizedDriverFields(driver);
        driverRepository.save(driver);

        /*
            * Subimos las fotos del carnet a R2 Storage y guardamos la información de los archivos en la base de datos.
            * Esto es necesario para que el sistema pueda acceder a las fotos del carnet cuando sea necesario, para la verificación de los carnets por parte de los administradores.
         */
        Media frontMedia = r2StorageService.uploadFile(frontPhoto, user, CategoryMediaEnum.LICENSE_FRONT);
            mediaRepository.save(frontMedia);

        Media backMedia = r2StorageService.uploadFile(backPhoto, user, CategoryMediaEnum.LICENSE_BACK);
            mediaRepository.save(backMedia);

        /*
         * Llamos al metodo provadi para actualizar el SecurityContextHolder.
         * Esto es necesario para que el usuario tenga acceso inmediato a los nuevos roles
         * asignados (en este caso, el rol de "DRIVER") sin necesidad de que el usuario
         * vuelva a iniciar sesión.
         */
        updateSecurityContext(user);

        /*
         * Se crea una nueva instancia de CustomUserDetails con el usuario actualizado.
         * Esto es necesario para que el token JWT contenga los roles actualizados del usuario.
         * Esto es importante porque el usuario puede haber cambiado de roles después de iniciar sesión
         */
        CustomUserDetails updatedUserDetails = new CustomUserDetails(user);

        String authoritiesJson;
        try {
            authoritiesJson = new ObjectMapper().writeValueAsString(updatedUserDetails.getAuthorities());
        } catch (JsonProcessingException e) {
            throw new ConflictException("Error al serializar autoridades para JWT: " + e.getMessage());
        }

        /*
         * Creamos los claims del token JWT.
         * Esto incluye las autoridades del usuario y el nombre de usuario.
         * Esto es necesario para que el token contenga la información necesaria
         * para la autorización y autenticación del usuario.
         */
        Claims claims = Jwts.claims()
            .add(AUTHORITIES_CLAIM, authoritiesJson)
            .add(USERNAME_CLAIM, updatedUserDetails.getUsername())
            .build();

        /*
         * Generamos el Access Token utilizando los métodos de JwtUtils.
         * Esto incluye la firma del token y la adición de los claims necesarios.
         */
        String accessToken = generateAccessToken(updatedUserDetails.getUsername(), claims);

        /*
         * Generamos el Refresh Token utilizando los mismos claims.
         * Esto es necesario para que el usuario pueda obtener un nuevo Access Token
         */
        String refreshToken = generateRefreshToken(updatedUserDetails.getUsername(), claims);

        /*
         * Creamos una instancia de TokenResponseDTO con los tokens generados.
         */
        TokenResponseDTO tokens = new TokenResponseDTO(accessToken, refreshToken);

        return ResponseUtils.buildOKResponse(List.of("El perfil de chofer ha sido creado correctamente."), tokens);
    }

    @Override
    public Response<DriverPendingPageResponseDTO> getPendingLicenses(int skip, String orderBy) {

        log.info("Obteniendo conductores con licencias pendientes. Skip: {}, OrderBy: {}", skip, orderBy);

        Page<Driver> page = driverRepository.findAllPendingLicenses(
            getPageable(orderBy, skip)
        );

        List<DriverPendingResponseDTO> drivers = page.getContent().stream().map(driver -> {

            String frontUrl = mediaRepository
                    .findByUserIdAndCategory(driver.getUser().getId(), CategoryMediaEnum.LICENSE_FRONT)
                    .map(mediaService::generatePresignedUrlPublic)
                    .orElse(null);

            String backUrl = mediaRepository
                    .findByUserIdAndCategory(driver.getUser().getId(), CategoryMediaEnum.LICENSE_BACK)
                    .map(mediaService::generatePresignedUrlPublic)
                    .orElse(null);

            return driverMapper.convertDriverToDriverPendingResponseDTO(driver, frontUrl, backUrl);

        }).toList();

        String message = drivers.isEmpty()
            ? "No hay carnets pendientes de verificación."
            : "Carnets pendientes obtenidos con éxito.";

        DriverPendingPageResponseDTO response = DriverPendingPageResponseDTO.builder()
            .total(page.getTotalElements())
            .drivers(drivers)
            .build();

        return ResponseUtils.buildOKResponse(List.of(message), response);
    }

    @Override
    @Transactional
    public Response<Void> verifyLicense(Long driverId, DriverLicenseVerifyRequestDTO dto) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Chofer no encontrado."));

        if (driver.getLicenseStatus() != LicenseStatusEnum.PENDING) {
            throw new ConflictException("El carnet de este chofer ya fue procesado.");
        }

        if (dto.getApproved()) {
            driver.setLicenseStatus(LicenseStatusEnum.APPROVED);
            driver.setRejectionReason(null);
            driverRepository.save(driver);
            notificationService.send(
                    driver.getUser(),
                    NotificationEventEnum.LICENSE_APPROVED,
                    driver);
        } else {

            driver.setLicenseStatus(LicenseStatusEnum.REJECTED);
            driver.setRejectionReason(dto.getRejectionReason());
            driverRepository.save(driver);
            notificationService.send(
                    driver.getUser(),
                    NotificationEventEnum.LICENSE_REJECTED,
                    driver);
        }

        return ResponseUtils.buildOKResponse(
            List.of(dto.getApproved() ? "Carnet aprobado con éxito." : "Carnet rechazado con éxito."),
                null);
    }


    @Override
    public Response<DriverResponseDTO> getMyDriverProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Driver driver = driverRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene perfil de chofer."));

        String frontUrl = mediaRepository
            .findByUserIdAndCategory(user.getId(), CategoryMediaEnum.LICENSE_FRONT)
            .map(mediaService::generatePresignedUrlPublic)
            .orElse(null
        );

        String backUrl = mediaRepository
            .findByUserIdAndCategory(user.getId(), CategoryMediaEnum.LICENSE_BACK)
            .map(mediaService::generatePresignedUrlPublic)
            .orElse(null
        );

        DriverResponseDTO response = driverMapper.convertDriverToDriverResponseDTO(
            driver,
            frontUrl,
            backUrl
        );

        return ResponseUtils.buildOKResponse( List.of("Perfil de chofer obtenido con éxito."), response);
    }

    /**
     * Metodo utilizado para verificar si el usuario ya tiene un perfil de chofer.
     * Si ya existe un perfil de chofer, se lanza una excepcion.
     *
     * @return void
     * @throws ConflictException si el usuario no se encuentra o ya existe un perfil de chofer.
     */
    private void checkIfDriverProfileExists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
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
     * @throws ConflictException si el rol "ROLE_DRIVER" no existe en la base de datos.
     */
    private void assignDriverRoleToUser(User user) {
        // Usamos la constante ROLE_DRIVER definida arriba
        Role driverRole = roleRepository.findByName(ROLE_DRIVER)
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

    /**
     * Metodo utilizado para normalizar los campos del chofer.
     * Este metodo se puede utilizar para realizar cualquier normalizacion de los campos del chofer.
     * @param driver
     * @return void
     */
    private void normalizedDriverFields(Driver driver){
        driver.setAddressStreet(driver.getAddressStreet().toUpperCase().trim());
    }

    /**
     * Metodo utilizado para obtener un objeto Pageable a partir de los parametros de ordenamiento y paginacion.
     * @param type tipo de ordenamiento (RECENT, OLD)
     * @param skip cantidad de registros a saltar para la paginacion
     * @return Pageable objeto Pageable con la configuracion de paginacion y ordenamiento
     */
    private Pageable getPageable(String type, int skip) {
        final int PAGE_SIZE = 10;
        int page = skip / PAGE_SIZE;

        Sort sort = switch (type) {
            case "RECENT" -> Sort.by("id").descending();
            case "OLD"    -> Sort.by("id").ascending();
            default       -> Sort.by("id").descending();
        };

        return PageRequest.of(page, PAGE_SIZE, sort);
    }
}
