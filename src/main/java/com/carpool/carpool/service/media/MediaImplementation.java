package com.carpool.carpool.service.media;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.carpool.carpool.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.carpool.carpool.dto.driver.LicenseUrlsResponse;
import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.media.MediaRepository;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.r2.IR2StorageService;
import com.carpool.carpool.utils.ResponseUtils;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class MediaImplementation implements IMediaService{

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaImplementation.class);

    private final IR2StorageService r2StorageImplementation;
    private final MediaRepository mediaRepository;
    private final S3Presigner s3Presigner;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp"
    );

    @Value("${cloudflare.r2.bucket-private}")
    private String bucket;

    @Value("${cloudflare.r2.bucket-public}")
    private String nameBucketPublic;

    @Value("${cloudflare.r2.public.endpoint}")
    private String publicEndpoint;

    private static final String FILENAME_DEFAULT_PHOTO = "default-profile.png";

    @Transactional
    public Response<String> getProfilePictureUrl() {
        Long idUser = getAuthenticatedUserId(); 
        
        Media media = mediaRepository.findByUserIdAndCategory(idUser, CategoryMediaEnum.PROFILE)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró foto de perfil para el usuario"));

        String presignedUrl = generatePresignedUrl(media);

        return ResponseUtils.buildOKResponse(List.of("URL de foto de perfil obtenida con éxito"), presignedUrl);
    }

    @Transactional(readOnly = true)
    public Response<LicenseUrlsResponse> getLicensePhotoUrls() {
        Long idUser = getAuthenticatedUserId();
 
        String frontUrl = null;
        String backUrl = null;
 
        Optional<Media> frontMedia = mediaRepository.findByUserIdAndCategory(
            idUser, CategoryMediaEnum.LICENSE_FRONT);
        if (frontMedia.isPresent()) {
            try {
                frontUrl = generatePresignedUrl(frontMedia.get());
            } catch (Exception e) {
                LOGGER.warn("Error generando URL para frente del carnet, usuario: {}", idUser, e);
            }
        }
 
        Optional<Media> backMedia = mediaRepository.findByUserIdAndCategory(
            idUser, CategoryMediaEnum.LICENSE_BACK);
        if (backMedia.isPresent()) {
            try {
                backUrl = generatePresignedUrl(backMedia.get());
            } catch (Exception e) {
                LOGGER.warn("Error generando URL para dorso del carnet, usuario: {}", idUser, e);
            }
        }
 
        if (frontUrl == null && backUrl == null) {
            throw new ResourceNotFoundException("No se encontraron fotos del carnet para el usuario");
        }
 
        LicenseUrlsResponse response = LicenseUrlsResponse.builder()
                .frontLicenseUrl(frontUrl)
                .backLicenseUrl(backUrl)
                .build();
 
        LOGGER.info("URLs del carnet obtenidas para usuario: {}", idUser);
        return ResponseUtils.buildOKResponse(
            List.of("URLs del carnet obtenidas con éxito"), response);
    }

    @Transactional
    public Response<Void> uploadMedia(MultipartFile frontFile, MultipartFile backFile, CategoryMediaEnum category) {
        Long idUser = getAuthenticatedUserId();
 
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
 
        if (category == CategoryMediaEnum.PROFILE) {

            if (frontFile == null || frontFile.isEmpty()) {
                throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
            }
            validateMediaFile(frontFile);
            processMediaUpload(frontFile, user, CategoryMediaEnum.PROFILE);
            
        } else if (category == CategoryMediaEnum.LICENSE_FRONT || category == CategoryMediaEnum.LICENSE_BACK) {
            // LICENSE: puede ser solo frente, solo dorso, o ambas
            if ((frontFile == null || frontFile.isEmpty()) && (backFile == null || backFile.isEmpty())) {
                throw new BadRequestException("Debe proporcionar al menos una foto (frente o dorso)");
            }
 
            // Procesar frente si se proporciona
            if (frontFile != null && !frontFile.isEmpty()) {
                validateMediaFile(frontFile);
                processMediaUpload(frontFile, user, CategoryMediaEnum.LICENSE_FRONT);
            }
 
            // Procesar dorso si se proporciona
            if (backFile != null && !backFile.isEmpty()) {
                validateMediaFile(backFile);
                processMediaUpload(backFile, user, CategoryMediaEnum.LICENSE_BACK);
            }
        }
 
        LOGGER.info("Media subido/actualizado ({}). Usuario: {}", category, idUser);
        return ResponseUtils.buildOKResponse(List.of("Archivo subido y almacenado con éxito"), null);
    }

    @Transactional
    public Response<Void> deleteMedia(CategoryMediaEnum category) {
        Long idUser = getAuthenticatedUserId();
 
        User user = userRepository.findById(idUser)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
 
        try {
            if (category == CategoryMediaEnum.PROFILE) {
                Optional<Media> customMedia = mediaRepository.findByUserIdAndCategory(
                    idUser, CategoryMediaEnum.PROFILE);
 
                if (customMedia.isPresent()) {
                    deleteMediaFile(customMedia.get());
                }
 
                // Guardar referencia a foto default
                saveDefaultProfilePicture(user);
                LOGGER.info("Perfil reestablecido a foto default para usuario: {}", idUser);
 
            } else if (category == CategoryMediaEnum.LICENSE_FRONT || category == CategoryMediaEnum.LICENSE_BACK) {
                //Eliminar las imagenes del carnet del usuari o..
                Optional<Media> frontMedia = mediaRepository.findByUserIdAndCategory(
                    idUser, CategoryMediaEnum.LICENSE_FRONT);
                if (frontMedia.isPresent()) {
                    deleteMediaFile(frontMedia.get());
                }
 
                Optional<Media> backMedia = mediaRepository.findByUserIdAndCategory(
                    idUser, CategoryMediaEnum.LICENSE_BACK);
                if (backMedia.isPresent()) {
                    deleteMediaFile(backMedia.get());
                }
 
                if (frontMedia.isEmpty() && backMedia.isEmpty()) {
                    throw new ResourceNotFoundException("No se encontraron fotos del carnet para eliminar");
                }
 
                LOGGER.info("Fotos del carnet eliminadas para usuario: {}", idUser);
            }
 
            return ResponseUtils.buildOKResponse(
                List.of("Archivo eliminado exitosamente"), null);
 
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Error de integridad en BD al eliminar media. Usuario: {}", idUser, e);
            throw new RuntimeException("Error de base de datos al eliminar el archivo", e);
        } catch (Exception e) {
            LOGGER.error("Error inesperado al eliminar media. Usuario: {}", idUser, e);
            throw new RuntimeException("Error al eliminar el archivo: " + e.getMessage());
        }
    }
    
    @Override
    public String getProfilePictureUrlByUserId(Long idUser) { 
    
        if (idUser == null) {
            LOGGER.warn("El ID de usuario proporcionado es nulo.");
            return null;
        }

        Optional<Media> mediaOptional = mediaRepository.findByUserIdAndCategory(idUser, CategoryMediaEnum.PROFILE);
        
        if (mediaOptional.isEmpty()) {
            LOGGER.warn("No se encontró foto de perfil personalizada para el usuario {}. Usando URL por defecto.", idUser);
            return null; 
        }
        
        Media media = mediaOptional.get();

       if (FILENAME_DEFAULT_PHOTO.equals(media.getObjectKey())) {
            return null;
        }
 
        try {
            return generatePresignedUrl(media);
        } catch (Exception e) {
            LOGGER.error("Error al generar URL presignada para usuario: {}", idUser, e);
            return null;
        }
    }

    @Override
    public Media buildMedia(User user, String bucket, CategoryMediaEnum category, 
                             String objectKey, String filename, String contentType, Long byteSize) {
        Media media = new Media();
        media.setUser(user);
        media.setBucket(bucket);
        media.setCategory(category);
        media.setObjectKey(objectKey);
        media.setFileName(filename);
        media.setContentType(contentType);
        media.setByteSize(byteSize);
        media.setCreatedAt(LocalDateTime.now());
        return media;
    }

    @Override
    @Transactional
    public void saveDefaultProfilePicture(User user) {
        Media defaultMedia = buildMedia(user, bucket, CategoryMediaEnum.PROFILE,
                FILENAME_DEFAULT_PHOTO, FILENAME_DEFAULT_PHOTO, "image/png", 4720L);
        mediaRepository.save(defaultMedia);
    }

    @Override
    public String generatePresignedUrlPublic(Media media) {
        return generatePresignedUrl(media);
    }


    private void processMediaUpload(MultipartFile file, User user, CategoryMediaEnum category) {
        Optional<Media> existingMedia = mediaRepository.findByUserIdAndCategory(user.getId(), category);
 
        if (existingMedia.isPresent()) {
            Media existing = existingMedia.get();
            String oldObjectKey = existing.getObjectKey();
 
            Media newMedia = r2StorageImplementation.uploadFile(file, user, category);
 
            existing.setObjectKey(newMedia.getObjectKey());
            existing.setFileName(newMedia.getFileName());
            existing.setContentType(newMedia.getContentType());
            existing.setByteSize(newMedia.getByteSize());
            existing.setUpdatedAt(LocalDateTime.now());
 
            mediaRepository.save(existing);
            
            //Eliminar el archivo existente para reemplazarlo. 
            try {
                r2StorageImplementation.deleteFile(oldObjectKey);
                LOGGER.info("Archivo anterior eliminado de R2: {}", oldObjectKey);
            } catch (Exception e) {
                LOGGER.warn("No se pudo eliminar archivo anterior de R2: {}", oldObjectKey, e);
            }
        } else {
            Media uploadedMedia = r2StorageImplementation.uploadFile(file, user, category);
            uploadedMedia.setCreatedAt(LocalDateTime.now());
            mediaRepository.save(uploadedMedia);
        }
    }

    private void validateMediaFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("La imagen supera el tamaño máximo de 2MB");
        }
 
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Formato no permitido. Solo PNG, JPG, JPEG, WEBP");
        }
    }

    /**
     * Elimina un archivo de la BD y de R2.
     * Método auxiliar para la eliminación.
     */
    private void deleteMediaFile(Media media) {
        String objectKey = media.getObjectKey();
        mediaRepository.delete(media);
        mediaRepository.flush();
 
        // No eliminar la foto default de R2
        if (!objectKey.equals(FILENAME_DEFAULT_PHOTO)) {
            try {
                r2StorageImplementation.deleteFile(objectKey);
                LOGGER.info("Archivo eliminado de R2: {}", objectKey);
            } catch (Exception e) {
                LOGGER.error("Error al eliminar archivo de R2: {}", objectKey, e);
                throw new RuntimeException("Error al eliminar archivo de R2: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene el ID del usuario autenticado en el contexto de seguridad. 
     * @return El ID del usuario autenticado en el contexto de seguridad.
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new ConflictException("Usuario autenticado no encontrado."))
            .getId();
    }

    /**
     * Metodo encargado de generar la url del archivo para que pueda ser accedido
     * @param media Objeto {@link Media}
     * @return Url del tipo {@link String}
     */
   private String generatePresignedUrl(Media media) {
        try {
            
            if (FILENAME_DEFAULT_PHOTO.equals(media.getObjectKey())) {
                return publicEndpoint + media.getObjectKey();
            }
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(media.getObjectKey())
                    .build();
 
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(24))
                    .getObjectRequest(getObjectRequest)
                    .build();
 
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
 
        } catch (Exception e) {
            LOGGER.error("Error al generar URL presignada para objectKey: {}", media.getObjectKey(), e);
            throw new RuntimeException("Error al generar URL de acceso: " + e.getMessage());
        }
    }

    // /**
    //  * Metodo que se encarga de eliminar el media personalizado de un usuario.
    //  * @param idUser Id del usuario del tipo {@link Long} (Se pasa internamente)
    //  * @return El objeto {@link Media} eliminado.
    //  */
    // private Media deleteCustomMedia(Long idUser) {
    //     Media media = mediaRepository.findByUserId(idUser)
    //         .orElseThrow(() -> new ResourceNotFoundException("No se encontró el archivo..."));
            
    //     mediaRepository.delete(media); 
    //     mediaRepository.flush(); 
    //     return media;
    // }
}