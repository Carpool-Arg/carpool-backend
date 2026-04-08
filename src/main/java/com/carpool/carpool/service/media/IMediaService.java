package com.carpool.carpool.service.media;

import com.carpool.carpool.dto.driver.LicenseUrlsResponse;
import com.carpool.carpool.enums.media.CategoryMediaEnum;
import com.carpool.carpool.model.media.Media;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface IMediaService {

   /**
    * Metodo encargado de realizar una peticion al R2 para obtener la URL de un archivo asociado a un usuario.
    * @return URL de la imagen de perfil del usuario logeado en ese momento. 
    */
    Response<String> getProfilePictureUrl();

    Response<LicenseUrlsResponse> getLicensePhotoUrls();

    /**
     * Se encarga de subir un archivo al R2 y guardar su referencia en la base de datos.
     * @param file Archivo a subir del tipo {@link MultipartFile}
     * @return {@link Response} sin data.
     */
    Response<Void> uploadMedia(MultipartFile frontFile, MultipartFile backFile, CategoryMediaEnum category);

    /**
     * Elimina el archivo de imagen de perfil del usuario autenticado y restaura la imagen por defecto.
     * @return {@link Response}
     */
    Response<Void> deleteMedia(CategoryMediaEnum category);

    /**
     * Obtiene la URL de la imagen de perfil por defecto de un usuario dado su ID.
     * @param idUser ID del usuario del tipo {@link Long}
     * @return URL de la imagen de perfil por defecto del tipo {@link String}       
     */
    String getProfilePictureUrlByUserId(Long idUser);

    /**
     * Construye el objeto media para la foto de perfil del usuario. 
     * @param user
     * @param bucket
     * @param category
     * @param objectKey
     * @param filename
     * @param contentType
     * @param byteSize
     * @return
     */
    Media buildMedia(User user, String bucket, CategoryMediaEnum category, 
                     String objectKey, String filename, String contentType, Long byteSize);
    /**
     * Crea la imagen por defecto del usuario. 
     * @param user
     */             
    void saveDefaultProfilePicture(User user);

    String generatePresignedUrlPublic(Media media);
}
