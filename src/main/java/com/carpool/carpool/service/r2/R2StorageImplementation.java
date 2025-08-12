package com.carpool.carpool.service.r2;

import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.model.user.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2StorageImplementation implements IR2StorageService{

    private static final Logger LOGGER = LoggerFactory.getLogger(R2StorageImplementation.class);

    private final S3Client r2S3Client;

    private final List<String> EXTENSION_ACCEPT = List.of(".jpg", ".jpeg", ".png", ".webp");

    @Value("${cloudflare.r2.bucket-private}")
    private String bucket;

    public Media uploadFile(MultipartFile file, Long ownerIdentification) {
        validateFile(file);
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String objectKey = generateObjectKey(fileExtension);
        String contentType = file.getContentType();

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            r2S3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            LOGGER.info("ARCHIVO SUBIDO EXITOSAMENTE AL R2: {}/{}", bucket, objectKey);
            User user = new User();
            user.setId(ownerIdentification);
            return createMediaRecord(user, bucket, objectKey,originalFilename, contentType, file.getSize());

        } catch (Exception e) {
            LOGGER.error("AL SUBIR EL ARCHIVO AL R2: {}", e.getMessage(), e);
            throw new RuntimeException("Error al subir archivo a R2", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            r2S3Client.deleteObject(deleteRequest);
            LOGGER.info("ARCHIVO ELIMINADO: {}/{}", bucket, objectKey);

        } catch (Exception e) {
            LOGGER.error("AL ELIMINAR EL ARCHIVO DEL R2: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Error al eliminar archivo de R2", e);
        }
    }

    public boolean fileExists(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            r2S3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            LOGGER.error("AL VERIFICAR LA EXISTENCIA DEL ARCHIVO DEL R2: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Error al verificar archivo de R2", e);
        }
    }

    /**
     * Metodo encargado de realizar las validaciones necesarias al archivo que se recibe. Para ello se valida que el archivo de tipo {@link MultipartFile} no sea
     * {@code null} o vacio, ya sea en su contenido-nombre, y que no supere el límite de 5mb.
     * @param file Archivo a subir del tipo {@link MultipartFile}
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo es requerido");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo excede el límite de 5MB");
        }
    }

    /**
     * Metodo encargado de validar la extension del archivo
     * @param file Archivo del tipo {@link MultipartFile}
     */
    private void validateImageFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/") || !EXTENSION_ACCEPT.contains(getFileExtension(file.getOriginalFilename()))) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
    }

    /**
     * Obtiene la extension de un archivo
     * @param filename Nombre del archivo del tipo {@link String}
     * @return Extension del archivo del tipo {@link String}
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Metodo encargado de crear la key-identificador unico del archivo que se va a subir al R2. Para ello, respeta la estructura de
     * prefijo_timestamp_uuid.extension
     * @param extension Extension del archivo de tipo {@link String}
     * @return Key del archivo de tipo {@link String}
     */
    private String generateObjectKey(String extension) {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());

        StringBuilder key = new StringBuilder();
        key.append(timestamp).append("_").append(uuid);
        if (extension != null && !extension.isEmpty()) {
            key.append(extension);
        }

        return key.toString();
    }

    /**
     * Metodo encargado de crear un objeto {@link Media}
     * @param user Propietario del tipo {@link User}
     * @param bucket Nombre del bucket del tipo {@link String}
     * @param objectKey Identificador unico del archivo en R2 del tipo {@link String}
     * @param filename Nombre del archivo {@link String}
     * @param contentType Tipo de contenido del archivo del tipo {@link String}
     * @param byteSize Tamanio del archivo del tipo {@link Long}
     * @return Objeto {@link Media}
     */
    private Media createMediaRecord(User user, String bucket, String objectKey, String filename, String contentType, Long byteSize) {
        Media media = new Media();
        media.setOwner(user);
        media.setBucket(bucket);
        media.setObjectKey(objectKey);
        media.setFileName(filename);
        media.setContentType(contentType);
        media.setByteSize(byteSize);
        media.setCreatedAt(LocalDateTime.now());

        return media;
    }
}
