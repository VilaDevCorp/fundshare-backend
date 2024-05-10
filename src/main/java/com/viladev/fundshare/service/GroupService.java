package com.viladev.fundshare.service;

import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.model.CustomUserDetail;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.utils.FilterUtils;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {

        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Group createGroup(String name, String description, User createdBy) throws EmptyFormFieldsException {
        if (name == null) {
            throw new EmptyFormFieldsException(null);
        }
        Group group = new Group(name, description, createdBy);
        return groupRepository.save(group);
    }

    public Group editGroup(UUID id, String name, String description)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {
        if (id == null) {
            throw new EmptyFormFieldsException(null);
        }
        Group group = groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        FilterUtils.checkIfCreator(group, SecurityContextHolder.getContext().getAuthentication().getName());

        if (name != null) {
            group.setName(name);
        }
        if (description != null) {
            group.setDescription(description);
        }
        return groupRepository.save(group);
    }

    public Group getGroup(UUID id) throws InstanceNotFoundException {
        return groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
    }

}
