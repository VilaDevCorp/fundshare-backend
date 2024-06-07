package com.viladev.fundshare.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.UserPayment;
import com.viladev.fundshare.model.dto.UserPaymentDto;

@Repository
public interface UserPaymentRepository extends JpaRepository<UserPayment, UUID> {

    @EntityGraph(value = "UserPayment.payment")
    Set<UserPayment> findByPaymentGroupIdAndUserUsername(UUID groupId, String username);

    @EntityGraph(value = "UserPayment.payment")
    @Query(value = "SELECT up FROM UserPayment up WHERE up.payment.group.id = :groupId AND (up.user.username = :username OR up.payment.createdBy.username = :username)")
    Set<UserPayment> findOperationsInGroupRelatedToUser(UUID groupId, String username);

    @Query("SELECT new com.viladev.fundshare.model.dto.UserPaymentDto(up, true) FROM UserPayment up " +
            "WHERE (:groupId is null OR up.payment.group.id = :groupId) AND " +
            "(:username is null OR (up.payment.createdBy.username = :username OR up.user.username = :username)) AND " +
            "(:relatedUsername is null OR (up.payment.createdBy.username = :relatedUsername OR up.user.username = :relatedUsername)) " +
            " ORDER BY up.payment.createdAt DESC")
    List<UserPaymentDto> findByGroupIdAndRelatedUser(UUID groupId, String username, String relatedUsername);

}
