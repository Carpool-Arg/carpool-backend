package com.carpool.carpool.service.user;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.carpool.carpool.enums.UserStatus;
import com.carpool.carpool.model.user.User;
import com.carpool.carpool.repository.user.UserAccountRepository;
import com.carpool.carpool.repository.user.UserRepository;

public class UserAccountServiceImpl implements IUserAccountService{

    //Definir la duracion del bloque en milisegundos, 900000 son 15 minutos
    private static final long LOCK_DURATION = 900000; 
    
    //Definir la cantidad de intentos que se le va a permitir al usuario, tanto como para suspender como para bloquear la cuenta
    public static final int SUSPEND_ATTEMPTS = 3;
    public static final int LOCK_ATTEMPTS = 5;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public void increaseFailedAttempts(User user) {
        int failedAttempts = user.getFailedAttempts() + 1;
        userAccountRepository.updateFailedAttempts(failedAttempts, user.getUsername());
    }

    @Override
    public void resetFailedAttempts(User user) {
        userAccountRepository.updateFailedAttempts(0, user.getUsername());
    }

    @Override
    public void lockAccount(User user) {
        user.setAccountStatus(UserStatus.LOCKED);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public void suspendAccount(User user) {
        increaseFailedAttempts(user);
        user.setAccountStatus(UserStatus.SUSPENDED);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public void unlockAccount(User user) {
        user.setAccountStatus(UserStatus.ACTIVE);
        user.setLockTime(null);
        user.setFailedAttempts(0);
        userRepository.save(user);
    }

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
}
