package com.template.backtemplate.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.template.backtemplate.model.ValidationCode;

public interface ValidationCodeRepository extends JpaRepository<ValidationCode, UUID> {

    List<ValidationCode> findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(String username,
            String type);

}
