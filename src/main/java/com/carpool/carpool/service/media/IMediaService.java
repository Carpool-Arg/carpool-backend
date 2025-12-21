package com.carpool.carpool.service.media;

import com.carpool.carpool.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface IMediaService {

   /**
    * Metodo encargado de realizar una peticion al R2 para obtener la URL de un archivo asociado a un usuario.
    * @return URL de la imagen de perfil del usuario logeado en ese momento. 
    */
    Response<String> getFileUser();

    /**
     * Se encarga de subir un archivo al R2 y guardar su referencia en la base de datos.
     * @param file Archivo a subir del tipo {@link MultipartFile}
     * @return {@link Response} sin data.
     */
    Response<Void> uploadAndSaveFileUser(MultipartFile file);

    /**
     * Elimina el archivo de imagen de perfil del usuario autenticado y restaura la imagen por defecto.
     * @return {@link Response}
     */
    Response<Void> deleteFileUser();

    /**
     * Obtiene la URL de la imagen de perfil por defecto de un usuario dado su ID.
     * @param idUser ID del usuario del tipo {@link Long}
     * @return URL de la imagen de perfil por defecto del tipo {@link String}       
     */
    String getProfilePictureUrlByUserId(Long idUser);
}
