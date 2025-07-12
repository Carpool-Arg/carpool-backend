package com.carpool.carpool.service.account;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carpool.carpool.enums.UserStatus;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.account.UserAccountRepository;
import com.carpool.carpool.repository.user.UserRepository;

import jakarta.transaction.Transactional;
@Service
public class UserAccountImplementation implements IUserAccountService{

    //Definir la duracion del bloqueo en milisegundos, 900000 son 15 minutos
    private static final long LOCK_DURATION = 900000; 

    //Definir la cantidad de tiempo para saber si resetear o no la cantidad de intentos fallidos del usuario
    private static final long FOUR_HOURS = 4 * 60 * 60 * 1000;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    UserRepository userRepository;

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
        user.setAccountStatus(UserStatus.SUSPENDED);
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
        user.setAccountStatus(UserStatus.ACTIVE);
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
        user.setAccountStatus(UserStatus.LOCKED);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    /**
     * Metodo para debloquear la cuenta de un usuario 
     * Aun no se usa, pero queda para mas adelante 
     * @param user
     */
    @Override
    @Transactional
    public void unlockAccount(User user) {
        user.setAccountStatus(UserStatus.ACTIVE);
        user.setLockTime(null);
        user.setFailedAttempts(0);
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

        if(user.getAccountStatus() == UserStatus.LOCKED){
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
