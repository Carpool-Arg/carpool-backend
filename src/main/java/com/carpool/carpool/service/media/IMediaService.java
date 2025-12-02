package com.carpool.carpool.service.media;

import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface IMediaService {

    /** TODO: se podria usar el mismo metodo para obtener recursos, habria que pasarle parametros y controlar en el service. Actualmente solo obtiene
     * foto de perfil, pero el dia de mañana que se desee implementar para reseñas, autos, etc solamente bastaria con validarlo en la implementacion.
     * Obtiene un archivo especifico, si es que existen
     * @param idUser Id del usuario del tipo {@link Long}
     * @return {@link Response} con data de la url del tipo {@link String}
     */
    Response<String> getFileUser(Long idUser);

    /**
     * Metodo encargado de realizar una peticion al R2 para almacenar un archivo y crear un registro en la base de datos
     * @param file Archivo del tipo {@link MultipartFile}
     * @param idUser Id del usuario propietario del tipo {@link Long}.
     * @return {@link Response} con data {@link Media}
     */
    Response<Void> uploadAndSaveFileUser(MultipartFile file, Long idUser);

    /**
     * Se encarga de eliminar un archivo tanto en R2 como en la base de datos.
     * @param idUser Id del usuario propietario del recurso del tipo {@link Long}
     */
    Response<Void> deleteFileUser(Long idUser);

    /**
     * Metodo encargado de obtener la URL de la foto de perfil de un usuario por su ID.
     * @param idUser Id del usuario del tipo {@link Long}
     * @return {@link String} con la URL del archivo.
     */
    String getProfilePictureUrlByUserId(Long idUser);
}
