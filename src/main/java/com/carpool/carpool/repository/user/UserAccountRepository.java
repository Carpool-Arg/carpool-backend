package com.carpool.carpool.repository.user;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserAccountRepository {
    @Query("update User u set u.failedAttempts=?1 where username=?2")
    @Modifying
    public void updateFailedAttempts(int attempt,String username); 

}
