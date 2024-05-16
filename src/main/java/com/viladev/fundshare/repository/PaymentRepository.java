package com.viladev.fundshare.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.viladev.fundshare.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(value = "Payment.userPayments")
    Optional<Payment> findById(UUID id);

    @EntityGraph(value = "Payment.userPayments")
    Set<Payment> findByGroupId(UUID groupId);

    @EntityGraph(value = "Payment.userPayments")
    Set<Payment> findByGroupIdAndCreatedByUsername(UUID groupId, String username);

}
