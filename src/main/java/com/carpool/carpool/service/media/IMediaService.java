package com.carpool.carpool.service.media;

import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.response.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IMediaService {
    /**
     * Metodo encargado de realizar una peticion al R2 para almacenar un archivo y de registrarlo en la base de datos
     * @param file Archivo del tipo {@link MultipartFile}
     * @param ownerIdentification Id del usuario propietario del tipo {@link Long}. En nuestro caso, es el DNI.
     * @return Objeto {@link Media}
     */
    Response<Media> uploadAndSaveFile(MultipartFile file, Long ownerIdentification);

    /**
     * Se encarga de eliminar un archivo tanto en R2 como en la base de datos.
     * @param mediaId Id de la media del tipo {@link Long}
     */
    Response<Void> deleteFile(Long mediaId);

    /**
     * Obtiene los archivos, si es que existen, de un usuario
     * @param ownerIdentification Id del propietario de la foto del tipo {@link Long}
     * @return {@link List} con objetos {@link Media}
     */
    Response<List<Media>> getFilesByOwner(Long ownerIdentification);

    /**
     * Obtiene un archivo especifico, si es que existen
     * @param mediaId Id del archivo {@link Long}
     * @return Objeto del tipo {@link Media}
     */
    Response<Media> getFileById(Long mediaId);
}
