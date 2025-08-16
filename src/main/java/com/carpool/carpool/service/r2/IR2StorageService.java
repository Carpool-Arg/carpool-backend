package com.carpool.carpool.service.r2;

import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.model.user.User;
import org.springframework.web.multipart.MultipartFile;


public interface IR2StorageService {

    /**
     * Sube un archivo al R2. Para ello valida el archivo, extrae datos del mismo y hace una peticion al R2 para el almacenamiento correspondiente.
     * @param file Archivo a subir al R2 del tipo {@link MultipartFile}
     * @param user Usuario del tipo {@link User}
     * @return Objeto {@link Media} con los datos correspondientes
     */
    Media uploadFile(MultipartFile file, User user, CategoryMediaEnum category);

    /**
     * Se encarga de eliminar un archivo del R2.
     * @param objectKey Identificador unico del archivo subido al R2 del tipo {@link String}
     */
    void deleteFile(String objectKey);

}
