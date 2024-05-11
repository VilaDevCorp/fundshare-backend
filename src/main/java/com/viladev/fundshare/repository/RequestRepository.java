package com.viladev.fundshare.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    Request findByGroupIdAndUserId(UUID groupId, UUID userId);
}
