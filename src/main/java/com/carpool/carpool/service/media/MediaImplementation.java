package com.carpool.carpool.service.media;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${cloudflare.r2.bucket-private}")
    private String bucket;

    @Value("${cloudflare.r2.bucket-public}")
    private String nameBucketPublic;

    private static final String FILENAME_DEFAULT_PHOTO = "default-profile.png";

    @Transactional
    public Response<String> getFileUser() {
        Long idUser = getAuthenticatedUserId(); 
        
        Media media = mediaRepository.findByUserId(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el archivo con el usuario indicado"));

        String presignedUrl = generatePresignedUrl(media);

        return ResponseUtils.buildOKResponse(List.of("Url del archivo obtenida con éxito") , presignedUrl);
    }

    @Transactional
    public Response<Void> uploadAndSaveFileUser(MultipartFile file) {
        Long idUser = getAuthenticatedUserId();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío."); 
        }
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Optional<Media> existMedia = mediaRepository.findByUserIdAndCategory(idUser, CategoryMediaEnum.PROFILE);

        Media media;
        if (existMedia.isPresent()) {
            Media mediaFound = existMedia.get();
            String oldObjectKey = mediaFound.getObjectKey();

            LOGGER.info("ACTUALIZANDO ARCHIVO EN LA BASE DE DATOS Y EN EL SERVIDOR, CON OBJECT KEY {}",mediaFound.getObjectKey());
            Media uploadMedia = r2StorageImplementation.uploadFile(file, user, CategoryMediaEnum.PROFILE);
            
            mediaFound.setObjectKey(uploadMedia.getObjectKey());
            mediaFound.setBucket(bucket);
            mediaFound.setFileName(uploadMedia.getFileName());
            mediaFound.setContentType(uploadMedia.getContentType());
            mediaFound.setByteSize(uploadMedia.getByteSize());
            mediaFound.setUpdatedAt(LocalDateTime.now());

            mediaRepository.save(mediaFound);

            try {
                r2StorageImplementation.deleteFile(oldObjectKey);
                LOGGER.info("ARCHIVO ANTERIOR ELIMINADO: {}", oldObjectKey);
            } catch (Exception e) {
                LOGGER.warn("No se pudo eliminar archivo anterior: {}", oldObjectKey, e);
            }
        }else{
            LOGGER.info("INSERTANDO NUEVO ARCHIVO EN LA BASE DE DATOS Y EN EL SERVIDOR");
            media = r2StorageImplementation.uploadFile(file, user, CategoryMediaEnum.PROFILE);
            mediaRepository.save(media);
        }
        return ResponseUtils.buildOKResponse(List.of("Archvo subido y almacenado con éxito") , null);
    }

    @Transactional
    public Response<Void> deleteFileUser() {
       Long idUser = getAuthenticatedUserId();
        
        User user = userRepository.findById(idUser)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        
        try {
            Media mediaToDelete = deleteCustomMedia(idUser);
            saveDefaultMedia(user);
            r2StorageImplementation.deleteFile(mediaToDelete.getObjectKey());
            
            LOGGER.info("ARCHIVO {} ELIMINADO DE R2. Perfil reestablecido a default.", mediaToDelete.getObjectKey());
            return ResponseUtils.buildOKResponse(List.of("Foto de perfil eliminada y reestablecida con éxito"), null);
            
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Error de DB al reestablecer perfil para User ID {}", idUser, e);
            throw new RuntimeException("Error de base de datos al reestablecer el perfil.", e);
        } catch (Exception e) {
            LOGGER.error("Error inesperado en deleteAndRestoreProfile para User ID {}", idUser, e);
            throw new RuntimeException("Error inesperado al reestablecer el perfil.", e);
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

        try {
            return generatePresignedUrl(media);
        } catch (Exception e) {
            LOGGER.error("Error al generar URL pre-firmada para el usuario {}", idUser, e);
            return null; 
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
    private String generatePresignedUrl(Media media){
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(media.getBucket())
                    .key(media.getObjectKey())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(24))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            LOGGER.error("ERROR AL GENERAR URL PREFIRMADA: {}", e);
            throw new RuntimeException("Error al generar URL de acceso");
        }
    }


    
    /**
     * Metodo que se encarga de eliminar el media personalizado de un usuario.
     * @param idUser Id del usuario del tipo {@link Long} (Se pasa internamente)
     * @return El objeto {@link Media} eliminado.
     */
    private Media deleteCustomMedia(Long idUser) {
        Media media = mediaRepository.findByUserId(idUser)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró el archivo..."));
            
        mediaRepository.delete(media); 
        mediaRepository.flush(); 
        return media;
    }

    /**
     * Metodo para guardar el media por defecto de un usuario.
     * @param user Objeto {@link User}
     * */
    private void saveDefaultMedia(User user) {
        Media defaultMedia = buildMedia(user, nameBucketPublic, CategoryMediaEnum.PROFILE,
            FILENAME_DEFAULT_PHOTO, FILENAME_DEFAULT_PHOTO, "image/png", 4720L);
        mediaRepository.save(defaultMedia);
    }


    /**
     * Metodo utilizado para consitruir el objeto media para el usuario. 
     * @param user 
     * @param bucket
     * @param category
     * @param objectKey
     * @param filename
     * @param contentType
     * @param byteSize
     * @return 
     */
    private Media buildMedia(User user, String bucket, CategoryMediaEnum category, 
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
}