package com.carpool.carpool.service.r2;

import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.media.MediaRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2StorageImplementation implements IR2StorageService{

    private static final Logger LOGGER = LoggerFactory.getLogger(R2StorageImplementation.class);

    private final S3Client r2S3Client;
    private final MediaRepository mediaRepository;

    private static final java.util.Set<String> ACCEPTED_EXTENSIONS = java.util.Set.of(".jpg", ".jpeg", ".png", ".webp");

    @Value("${cloudflare.r2.bucket-private}")
    private String bucket;

    public Media uploadFile(MultipartFile file, User user, CategoryMediaEnum category) {
        validateFile(file);

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String objectKey = generateObjectKey(user, category, fileExtension);

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            r2S3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            LOGGER.info("ARCHIVO SUBIDO EXITOSAMENTE AL R2: {}/{} | Categoría: {} | Usuario: {}", bucket, objectKey, category, user.getId());

            return buildMedia(user, bucket, category, objectKey, originalFilename, contentType, file.getSize());
        } catch (Exception e) {
            LOGGER.error("AL SUBIR EL ARCHIVO AL R2", e);
            throw new RuntimeException("Error al subir archivo a R2");
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
            throw new RuntimeException("Error al eliminar archivo de R2");
        }
    }

    /**
     * Metodo encargado de realizar las validaciones necesarias al archivo que se recibe. Para ello se valida que el archivo de tipo {@link MultipartFile} no sea
     * {@code null} o vacio, ya sea en su contenido-nombre, que no supere el límite de 5mb y que cuente con un tipo de contenido valido.
     * @param file Archivo a subir del tipo {@link MultipartFile}
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo es requerido");
        }
        String fileExtension = getFileExtension(originalName);
        if(fileExtension == null || fileExtension.isEmpty()){
            throw new IllegalArgumentException("El archivo a subir debe tener una extensión");
        }
        if (!ACCEPTED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Extensión no permitida");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo excede el límite de 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
    }

    /**
     * Obtiene la extension de un archivo y lo convierte a minusculas
     * @param filename Nombre del archivo del tipo {@link String}
     * @return Extension del archivo del tipo {@link String}
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot).toLowerCase() : "";
    }

    /**
     * Metodo encargado de crear la key-identificador unico del archivo que se va a subir al R2. Para ello, respeta la estructura de
     * prefijo_timestamp_uuid.extension
     * @param prefix Prefijo que se va a asignar al archivo. En este caso es el nombre del archivo del tipo {@link String}
     * @param extension Extension del archivo de tipo {@link String}
     * @return Key del archivo de tipo {@link String}
     */
    private String generateObjectKey(User user, CategoryMediaEnum category, String extension) {
        String uuid = UUID.randomUUID().toString();
        String categoryPrefix = getCategoryPrefix(category);
        String baseKey = String.format("%s/user/%d/%s%s", categoryPrefix, user.getId(), uuid, extension);
 
        // Validar unicidad en BD
        while (mediaRepository.existsByObjectKey(baseKey)) {
            uuid = UUID.randomUUID().toString();
            baseKey = String.format("%s/user/%d/%s%s", categoryPrefix, user.getId(), uuid, extension);
        }
 
        return baseKey;
    }

    /**
     * Obtiene el prefijo de categoría para la estructura de objectKey.
     * 
     * @param category Categoría del archivo
     * @return Prefijo en minúsculas (ej: "profile", "license")
     */
    private String getCategoryPrefix(CategoryMediaEnum category) {
        return switch (category) {
            case PROFILE -> "profile";
            case LICENSE_FRONT, LICENSE_BACK -> "license";
            default -> throw new IllegalArgumentException("Categoría no soportada: " + category);
        };
    }

    // /**
    //  * Metodo encargado de recortar la extension del nombre de un archivo
    //  * @param nameFile Nombre del archivo del tipo {@link String}
    //  * @return Nombre de archivo sin extension del tipo {@link String}
    //  */
    // private String stripExtension(String nameFile) {
    //     if (nameFile == null) return "";
    //     int dot = nameFile.lastIndexOf(".");
    //     if(dot <= 0) return nameFile;
    //     return nameFile.substring(0, dot);
    // }

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
    private Media buildMedia(User user, String bucket, CategoryMediaEnum category, String objectKey, String filename, String contentType, Long byteSize) {
        Media media = new Media();
        media.setUser(user);
        media.setBucket(bucket);
        media.setCategory(category);
        media.setObjectKey(objectKey);
        media.setFileName(filename);
        media.setContentType(contentType);
        media.setByteSize(byteSize);

        return media;
    }
}
