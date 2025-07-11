package com.carpool.carpool.service.user.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.security.model.CustomUserDetails;

/**
 * Clase encargada de realizar validaciones al usuario que solicita acceso a la aplicación para poder autorizar al mismo.
 */
@Service
public class UserDetailsImplementation implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    /**
     * Método que se encarga de obtener el usuario (en base al nombre del usuario recibido por parámetro) en la base de datos y retornar
     * un objeto {@link UserDetails} que contiene la información necesaria para autenticar al usuario.
     *
     * @param username El nombre del usuario a buscar.
     * @return Objeto {@link UserDetails}.
     * @throws UsernameNotFoundException En caso de que no se encuentre al usuario en la base de datos.
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userDB = userRepository.findByUsername(username)
                .orElseThrow(() ->  new UsernameNotFoundException(
                        String.format("El nombre de usuario %s no existe en el sistema!", username)));

        return new CustomUserDetails(userDB);
    }

}
