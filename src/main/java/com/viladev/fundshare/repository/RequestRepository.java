package com.viladev.fundshare.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.dto.RequestDto;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    Request findByGroupIdAndUserId(UUID groupId, UUID userId);

    @Query("SELECT new com.viladev.fundshare.model.dto.RequestDto(r) FROM Request r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    Slice<RequestDto> findByUserIdOrderByCreatedAt(UUID userId, Pageable pageable);

    @Query("SELECT new com.viladev.fundshare.model.dto.RequestDto(r) FROM Request r WHERE r.group.id = :groupId ORDER BY r.createdAt DESC")
    Slice<RequestDto> findByGroupId(UUID groupId, Pageable pageable);


}
