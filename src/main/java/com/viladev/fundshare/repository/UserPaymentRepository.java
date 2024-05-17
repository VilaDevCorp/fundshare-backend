package com.viladev.fundshare.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.UserPayment;

@Repository
public interface UserPaymentRepository extends JpaRepository<UserPayment, UUID> {

    @EntityGraph(value = "UserPayment.payment")
    Set<UserPayment> findByPaymentGroupIdAndUserUsername(UUID groupId, String username);

    @EntityGraph(value = "UserPayment.payment")
    @Query(value = "SELECT up FROM UserPayment up WHERE up.payment.group.id = :groupId AND (up.user.username = :username OR up.payment.createdBy.username = :username)")
    Set<UserPayment> findOperationsInGroupRelatedToUser(UUID groupId, String username);
}
