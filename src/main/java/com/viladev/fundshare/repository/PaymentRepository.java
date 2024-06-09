package com.viladev.fundshare.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.dto.PaymentDto;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(value = "Payment.userPayments")
    Optional<Payment> findById(UUID id);

    @Query("SELECT new com.viladev.fundshare.model.dto.PaymentDto(p) FROM Payment p WHERE (:groupId is null OR p.group.id = :groupId) AND (:username is null OR p.createdBy.username = :username) ORDER BY p.createdAt DESC")
    Slice<PaymentDto> findByGroupIdAndCreatedByUsername(UUID groupId, String username, Pageable pageable);

    @Query("SELECT new com.viladev.fundshare.model.dto.PaymentDto(p) FROM Payment p WHERE  (:username is null OR p.createdBy.username = :username) OR :username IN (select up.user.username from UserPayment up where up.payment.id = p.id) ORDER BY p.createdAt DESC")
    Slice<PaymentDto> findByRelatedUsername(String username, Pageable pageable);

}
