package com.viladev.fundshare.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.UserPayment;

@Repository
public interface UserPaymentRepository extends JpaRepository<UserPayment, UUID> {

    java.util.Set<UserPayment> findByPaymentGroupId(UUID groupId);
}
