package com.carpool.carpool.service.user.account;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.carpool.carpool.dto.user.ChangePasswordRequestDTO;
import com.carpool.carpool.enums.token.TokenStateEnum;
import com.carpool.carpool.enums.token.TokenTypeEnum;
import com.carpool.carpool.enums.user.UserStateEnum;
import com.carpool.carpool.exception.ConflictException;
import com.carpool.carpool.exception.ResourceNotFoundException;
import com.carpool.carpool.model.user.UserToken;
import com.carpool.carpool.repository.user.token.UserTokenRepository;
import com.carpool.carpool.response.Response;
import com.carpool.carpool.service.email.IEmailService;
import com.carpool.carpool.utils.PasswordUtils;
import com.carpool.carpool.utils.ResponseUtils;
import com.carpool.carpool.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserRepository;
import com.carpool.carpool.repository.user.account.UserAccountRepository;

import jakarta.transaction.Transactional;

import static com.carpool.carpool.utils.EmailMessageUtils.*;
import static com.carpool.carpool.utils.EmailMessageUtils.MESSAGE_FOOTER_WELCOME;

@Service
@RequiredArgsConstructor
public class UserAccountImplementation implements IUserAccountService{

    //Definir la duracion del bloqueo en milisegundos, 900000 son 15 minutos
    private static final long LOCK_DURATION = 900000;

    //Definir la cantidad de tiempo para saber si resetear o no la cantidad de intentos fallidos del usuario
    private static final long FOUR_HOURS = 4 * 60 * 60 * 1000;

    private final IEmailService emailImplementation;
    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Metodo para aumentar la cantidad de intentos de inicio de sesion fallidos de un usuario
     * @param user
     */
    @Override
    @Transactional
    public void increaseFailedAttempts(User user) {
        int failedAttempts = user.getFailedAttempts() + 1;
        userAccountRepository.updateFailedAttempts(failedAttempts, user.getUsername());
    }

    @Override
    @Transactional
    public Response<Void> unlockAccount(ChangePasswordRequestDTO changePasswordRequestDTO) {

        PasswordUtils.passwordsMatch(changePasswordRequestDTO.getPassword(), changePasswordRequestDTO.getConfirmPassword());

        UserToken tokenValidate = userTokenRepository.findByToken(changePasswordRequestDTO.getToken()).
                orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if (!TokenUtils.validateUserToken(tokenValidate, TokenTypeEnum.ACTIVATION)) throw new ConflictException("Token inválido");

        if (tokenValidate.isExpired()){
            tokenValidate.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(tokenValidate);
            throw new ConflictException("Token Expirado");
        }

        User user = userRepository.findById(tokenValidate.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if(!UserStateEnum.LOCKED.equals(user.getStatus())){
            throw new ConflictException("Su cuenta no se encuentra bloqueada");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequestDTO.getPassword()));
        user.setStatus(UserStateEnum.ACTIVE);
        user.setFailedAttempts(0);
        user.setLastFailedLoginTime(null);

        tokenValidate.setState(TokenStateEnum.USED);
        tokenValidate.setUsedAt(LocalDateTime.now());

        userTokenRepository.save(tokenValidate);
        userRepository.save(user);

        return ResponseUtils.buildOKResponse(List.of("Usuario activado con éxito") , null);
    }

    @Override
    @Transactional
    public Response<Void> activateAccount(String token){
        UserToken tokenValidate = userTokenRepository.findByToken(token).
                orElseThrow(() -> new ResourceNotFoundException("Token no encontrado"));

        if(!TokenTypeEnum.ACTIVATION.equals(tokenValidate.getType()) || !TokenStateEnum.PENDING.equals(tokenValidate.getState())){
            throw new ConflictException("El token es inválido");
        }

        if (tokenValidate.isExpired()){
            tokenValidate.setState(TokenStateEnum.EXPIRED);
            userTokenRepository.save(tokenValidate);
            throw new ConflictException("Token Expirado");
        }

        User user = userRepository.findById(tokenValidate.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if(!UserStateEnum.PENDING_VERIFICATION.equals(user.getStatus())){
            throw new ConflictException("La cuenta no puede ser activada en su estado actual");
        }

        user.setStatus(UserStateEnum.ACTIVE);
        tokenValidate.setState(TokenStateEnum.USED);
        tokenValidate.setUsedAt(LocalDateTime.now());

        userTokenRepository.save(tokenValidate);
        userRepository.save(user);

        emailImplementation.sendEmail(user.getEmail(), SUBJECT_EMAIL_WELCOME, TITLE_WELCOME.replace("{name}", user.getName()), MESSAGE_EMAIL_WELCOME, null,null, null, MESSAGE_FOOTER_WELCOME);

        return ResponseUtils.buildOKResponse(List.of("Usuario activado con éxito") , null);
    }

    /**
     * Metodo para volver a 0 la cantidad de intentos de inicio de sesion fallidos del usuario 
     * @param user
     */
    @Override
    @Transactional
    public void resetFailedAttempts(User user) {
        userAccountRepository.updateFailedAttempts(0, user.getUsername());
    }

    /**
     * Meotodo para suspender la cuenta de un usuario. Se setea el locktime en la fecha y hora actual
     * y se cambia el estado de la cuenta
     * @param user
     */
    @Override
    @Transactional
    public void suspendAccount(User user) {
        user.setStatus(UserStateEnum.SUSPENDED);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    /**
     * Metodo para de-suspender la cuenta de un usuario. Se setea el locktime en null
     * la cuenta vuelve a estar activa, pero la cantidad de intentos fallidos no se resetea
     * @param user
     */
    @Override
    @Transactional
    public void unSuspendAccount(User user){
        user.setStatus(UserStateEnum.ACTIVE);
        user.setLockTime(null);
        userRepository.save(user);
    }

    /**
     * Metodo para bloquear la cuenta de un usuario
     * @param user
     */
    @Override
    @Transactional
    public void lockAccount(User user) {
        user.setStatus(UserStateEnum.LOCKED);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    /**
     * Metodo para determinar si el tiempo de suspension de una cuenta (15 minutos) ha pasado
     * @param user
     * @return true si el tiempo paso, false si no 
     */
    @Override
    public boolean lockTimeExpired(User user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if(user.getStatus() == UserStateEnum.LOCKED){
            return false;
        }else if(lockTimeInMillis + LOCK_DURATION < currentTimeInMillis){
            return true;
        }

        return false;
    }

    /**
     * Meotodo para determinar si han pasado 4 horas o mas desde el ultimo incio de sesion fallido 
     * para resetear o no la cantidad de intentos del usuario
     * @param user
     * @return true si pasaron las 4 horas, false si no
     */
    @Override
    public boolean resetTimeExpired(User user) {
        long currentTimeInMillis = System.currentTimeMillis();
        return user.getLastFailedLoginTime() != null && (currentTimeInMillis - user.getLastFailedLoginTime().getTime() >= FOUR_HOURS);
    }


}
