package com.viladev.fundshare.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.GroupUser;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, UUID> {

    boolean existsByGroupIdAndUserUsername(UUID groupId, String username);

    GroupUser findByGroupIdAndUserUsername(UUID groupId, String username);
}
