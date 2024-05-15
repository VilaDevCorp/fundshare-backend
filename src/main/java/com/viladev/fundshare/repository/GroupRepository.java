package com.viladev.fundshare.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viladev.fundshare.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    @EntityGraph(value = "Group.users")
    Optional<Group> findById(UUID id);

}
