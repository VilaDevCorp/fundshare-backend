package com.viladev.fundshare.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.GroupUser;
import com.viladev.fundshare.model.dto.UserDto;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, UUID> {

    boolean existsByGroupIdAndUserUsername(UUID groupId, String username);

    GroupUser findByGroupIdAndUserUsername(UUID groupId, String username);

    @Query("select distinct new com.viladev.fundshare.model.dto.UserDto(gu.user) " +
            "from GroupUser gu " +
            "where gu.group in " +
            "   (select gu2.group from GroupUser gu2 where gu2.user.username = :username) " +
            "and gu.user.username != :username " +
            "and gu.group.id != :groupId " +
            "and gu.user.id not IN (select gu3.user.id from GroupUser gu3 " +
                                "where gu3.group.id = :groupId) " +
            "and not exists (select r from Request r " +
                                "where r.group.id = :groupId AND r.user.username = gu.user.username) " +
            "order by gu.user.username asc")
    Slice<UserDto> findRelatedUsers(String username, UUID groupId, Pageable pageable);

}
