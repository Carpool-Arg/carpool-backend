package com.carpool.carpool.service.r2;

import com.carpool.carpool.model.media.Media;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IR2StorageService {

    /**
     * Sube un archivo al R2. Para ello valida el archivo, extrae datos del mismo y hace una peticion al R2 para el almacenamiento correspondiente.
     * @param file Archivo a subir al R2 del tipo {@link MultipartFile}
     * @param ownerIdentification Id del propietario de la foto del tipo {@link Long}
     * @return Objeto {@link Media} con los datos correspondientes
     */
    Media uploadFile(MultipartFile file, Long ownerIdentification);

    /**
     * Se encarga de eliminar un archivo del R2.
     * @param objectKey Identificador unico del archivo subido al R2 del tipo {@link String}
     */
    void deleteFile(String objectKey);

    /**
     * Verifica si existe un archivo subido al R2
     * @param objectKey Identificador unico del archivo subido al R2 del tipo {@link String}
     * @return {@code true} si el archivo existe en R2, {@code false} caso contrario
     */
    boolean fileExists(String objectKey);
}
