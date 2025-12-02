package com.carpool.carpool.service.media;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.carpool.carpool.enums.media.CategoryMediaEnum;
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

    @Transactional
    public Response<String> getFileUser(Long idUser) {
        Media media = mediaRepository.findByUserId(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el archivo con el usuario indicado"));

        String presignedUrl = generatePresignedUrl(media);

        return ResponseUtils.buildOKResponse(List.of("Url del archivo obtenida con éxito") , presignedUrl);
    }

    public Response<Void> uploadAndSaveFileUser(MultipartFile file, Long idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Optional<Media> existMedia = mediaRepository.findByUserIdAndCategory(idUser, CategoryMediaEnum.PROFILE);

        Media media;
        // Ya existe, por ende se actualiza la imagen
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

    public Response<Void> deleteFileUser(Long idUser) {
        Media media = mediaRepository.findByUserId(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el archivo con el usuario indicado"));

        try {
            r2StorageImplementation.deleteFile(media.getObjectKey());
            mediaRepository.delete(media);

            LOGGER.info("ARCHIVO ELIMINADO: ID {}, bucket: {}, key: {}",
                    media.getId(), media.getBucket(), media.getObjectKey());
            return ResponseUtils.buildOKResponse(List.of("Archvo eliminado con éxito") , null);
        } catch (Exception e) {
            LOGGER.error("AL ELIMINAR EL ARCHIVO CON ID {}", media.getId(), e);
            throw new RuntimeException("Error al eliminar archivo");
        }
    }
    
    @Override
    public String getProfilePictureUrlByUserId(Long idUser) {
        Optional<Media> mediaOptional = mediaRepository.findByUserIdAndCategory(idUser, CategoryMediaEnum.PROFILE);
                
        if (mediaOptional.isEmpty()) {
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
}
