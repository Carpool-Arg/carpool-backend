package com.carpool.carpool.repository.user.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.carpool.carpool.model.user.User;


@Repository
public interface UserAccountRepository extends JpaRepository<User,Long>{
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts=?1 where username=?2")
    public void updateFailedAttempts(int attempt,String username); 

}
