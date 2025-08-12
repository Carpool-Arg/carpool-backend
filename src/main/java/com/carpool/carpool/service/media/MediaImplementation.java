package com.carpool.carpool.service.media;

import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.repository.media.MediaRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.r2.IR2StorageService;

import com.carpool.carpool.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaImplementation implements IMediaService{

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaImplementation.class);

    private final IR2StorageService r2StorageImplementation;
    private final MediaRepository mediaRepository;

    public Response<Media> uploadAndSaveFile(MultipartFile file, Long ownerIdentification) {
        Media media = r2StorageImplementation.uploadFile(file, ownerIdentification);
        mediaRepository.save(media);
        return ResponseUtils.buildOKResponse(List.of("Archvo subido y almacenado con éxito") , media);
    }

    public Response<Void> deleteFile(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media no encontrada con ID: " + mediaId));

        try {
            r2StorageImplementation.deleteFile(media.getObjectKey());
            mediaRepository.delete(media);

            LOGGER.info("ARCHIVO ELIMINADO: ID {}, bucket: {}, key: {}",
                    mediaId, media.getBucket(), media.getObjectKey());
            return ResponseUtils.buildOKResponse(List.of("Archvo eliminado con éxito") , null);
        } catch (Exception e) {
            LOGGER.error("AL ELIMINAR EL ARCHIVO CON ID {}", mediaId, e);
            throw new RuntimeException("Error al eliminar archivo", e);
        }
    }

    @Transactional
    public Response<List<Media>> getFilesByOwner(Long ownerIdentification) {
        List<Media> files = mediaRepository.findByOwnerOrderByCreatedAtDesc(ownerIdentification)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontraron archivos para el usuario: "+ownerIdentification));
        return ResponseUtils.buildOKResponse(List.of("Archivos del usuario obtenido con éxito") , files);
    }

    @Transactional
    public Response<Media> getFileById(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el archivo"));

        return ResponseUtils.buildOKResponse(List.of("Archivos del usuario obtenido con éxito") , media);
    }
}
