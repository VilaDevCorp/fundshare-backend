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
			+ " (SELECT coalesce(sum(up.amount),0) from UserPayment up WHERE up.payment.group = g AND up.payment.createdBy = :user )"
			+ "-(SELECT coalesce(sum(up.amount),0) from UserPayment up WHERE up.payment.group = g AND up.user = :user ))"
			+ "FROM Group g WHERE "
			+ " (:user IN (select gu.user from GroupUser gu where gu.group = g) ) AND "
			+ "	("
			+ "		:keyword is null OR "
			+ "     ("
			+ "			lower(g.name) like :keyword OR "
			+ "			lower(g.description) like :keyword"
			+ "     )"
			+ "	)"
			+ "ORDER BY g.createdAt DESC")
	Slice<GroupDto> advancedSearch(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

}
