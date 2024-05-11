package com.viladev.fundshare.service;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.KickedCreatorException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.UserAlreadyInvitedException;
import com.viladev.fundshare.exceptions.UserAlreadyPresentException;
import com.viladev.fundshare.exceptions.UserKickedIsNotMember;
import com.viladev.fundshare.model.CustomUserDetail;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.RequestRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.utils.FilterUtils;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final RequestRepository requestRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
            RequestRepository requestRepository) {

        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    public Group createGroup(String name, String description) throws EmptyFormFieldsException {
        User creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (name == null) {
            throw new EmptyFormFieldsException(null);
        }
        Group group = new Group(name, description, creator);
        group.setUsers(List.of(creator));
        return groupRepository.save(group);
    }

    public Group editGroup(UUID id, String name, String description)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {
        if (id == null) {
            throw new EmptyFormFieldsException(null);
        }
        Group group = groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        FilterUtils.checkIfCreator(group);

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

    public Request createRequest(UUID groupId, String username)
            throws InstanceNotFoundException, NotAllowedResourceException, UserAlreadyPresentException,
            UserAlreadyInvitedException, EmptyFormFieldsException {
        if (groupId == null || username == null) {
            throw new EmptyFormFieldsException(null);
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException("User not found");
        }
        if (group.getUsers().contains(user)) {
            throw new UserAlreadyPresentException("User already present in the group");
        }
        if (requestRepository.findByGroupIdAndUserId(groupId, user.getId()) != null) {
            throw new UserAlreadyInvitedException("User already invited to the group");
        }
        FilterUtils.checkIfCreator(group);
        Request request = new Request(group, user);
        return request;
    }

    public void kickUser(UUID groupId, String username)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException,
            KickedCreatorException, UserKickedIsNotMember {
        if (groupId == null || username == null) {
            throw new EmptyFormFieldsException(null);
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));
        FilterUtils.checkIfCreator(group);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException("User not found");
        }
        if (group.getCreatedBy().equals(user)) {
            throw new KickedCreatorException(null);
        }
        if (!group.getUsers().contains(user)) {
            throw new UserKickedIsNotMember(null);
        }
        group.getUsers().remove(user);
        groupRepository.save(group);
    }

    public void respondRequest(UUID requestId, boolean accept)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        if (requestId == null) {
            throw new EmptyFormFieldsException(null);
        }
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new InstanceNotFoundException("Request not found"));
        if (!FilterUtils.checkIfLoggedUser(request.getUser())) {
            throw new NotAllowedResourceException("You cannot respond other user's requests");
        }
        if (accept) {
            request.getGroup().getUsers().add(request.getUser());
            groupRepository.save(request.getGroup());
        }
        requestRepository.delete(request);
    }

}
