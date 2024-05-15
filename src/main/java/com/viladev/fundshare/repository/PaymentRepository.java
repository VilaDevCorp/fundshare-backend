package com.viladev.fundshare.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viladev.fundshare.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Set<Payment> findByGroupId(UUID groupId);

}
