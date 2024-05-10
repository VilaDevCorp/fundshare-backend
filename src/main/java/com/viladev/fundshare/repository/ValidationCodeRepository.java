package com.viladev.fundshare.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viladev.fundshare.model.ValidationCode;

public interface ValidationCodeRepository extends JpaRepository<ValidationCode, UUID> {

    List<ValidationCode> findByUserUsernameAndTypeOrderByCreatedAtDesc(String username,
            String type);

}
