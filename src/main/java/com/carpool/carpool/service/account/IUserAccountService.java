package com.carpool.carpool.service.account;

import com.carpool.carpool.model.user.User;

public interface IUserAccountService {
    /**
     * Metodo para aumentar la cantidad de intentos de inicio de sesion fallidos de un usuario
     * @param user
     */
    public void increaseFailedAttempts(User user);

    /**
     * Metodo para volver a 0 la cantidad de intentos de inicio de sesion fallidos del usuario 
     * @param user 
     */
    public void resetFailedAttempts(User user);

    /**
     * Meotodo para suspender la cuenta de un usuario. 
     * @param user
     */
    public void suspendAccount(User user);

    /**
     * Metodo para de-suspender la cuenta de un usuario
     * @param user
     */
    public void unSuspendAccount(User user);

    /**
     * Metodo para bloquear la cuenta de un usuario
     * @param user
     */
    public void lockAccount(User user);

    /**
     * Metodo para debloquear la cuenta de un usuario 
     * Aun no se usa, pero queda para mas adelante 
     * @param user
     */
    public void unlockAccount(User user);

    /**
     * Metodo para determinar si el tiempo de suspension de una cuenta (15 minutos) ha pasado
     * @param user
     * @return true si el tiempo paso, false si no 
     */
    public boolean lockTimeExpired(User user);

    /**
     * Meotodo para determinar si han pasado 4 horas o mas desde el ultimo incio de sesion fallido 
     * para resetear o no la cantidad de intentos del usuario
     * @param user
     * @return true si pasaron las 4 horas, false si no
     */
    public boolean resetTimeExpired(User user);
}
