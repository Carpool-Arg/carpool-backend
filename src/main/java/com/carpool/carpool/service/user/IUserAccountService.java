package com.carpool.carpool.service.user;

import com.carpool.carpool.model.user.User;

public interface IUserAccountService {
    public void increaseFailedAttempts(User user);

    public void resetFailedAttempts(User user);

    public void suspendAccount(User user);

    public void lockAccount(User user);

    public void unlockAccount(User user);

    public boolean lockTimeExpired(User user);
}
