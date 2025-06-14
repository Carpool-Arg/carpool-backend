package com.carpool.carpool.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carpool.carpool.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
