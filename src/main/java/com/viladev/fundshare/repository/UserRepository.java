package com.viladev.fundshare.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);

    User findByUsername(String username);

    User findByUsernameAndValidatedTrue(String email);
}
