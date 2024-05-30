package com.viladev.fundshare.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.dto.GroupDto;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    @EntityGraph(value = "Group.users")
    Optional<Group> findById(UUID id);

    @Query("SELECT new com.viladev.fundshare.model.dto.GroupDto(g, "
            + "(SELECT sum(p.amount) from UserPayment p WHERE p.payment.group = g AND p.user = :user )"
            + " - (SELECT sum(p.amount) from UserPayment p WHERE p.payment.group = g AND p.payment.createdBy = :user ))"
            + "FROM Group g WHERE "
            + ":user member of g.users AND " +
            "(:keyword is null OR ( lower(g.name) like lower(%:keyword%) OR lower(g.description) like lower(%:keyword%) )   )")
    Slice<GroupDto> advancedSearch(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

}
