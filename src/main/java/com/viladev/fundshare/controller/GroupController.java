package com.viladev.fundshare.controller;

import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.KickedCreatorException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.UserAlreadyInvitedException;
import com.viladev.fundshare.exceptions.UserAlreadyPresentException;
import com.viladev.fundshare.exceptions.UserKickedIsNotMember;
import com.viladev.fundshare.forms.GroupForm;
import com.viladev.fundshare.forms.RequestForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.service.GroupService;
import com.viladev.fundshare.service.UserService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;

@RestController
@RequestMapping("/api")
public class GroupController {

    private final UserService userService;
    private final GroupService groupService;

    @Autowired
    public GroupController(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @PostMapping("/group")
    public ResponseEntity<ApiResponse<Group>> createGroup(@RequestBody GroupForm groupForm)
            throws InstanceNotFoundException, EmptyFormFieldsException {
        ResponseEntity<ApiResponse<Group>> response = null;
        Group newGroup = groupService.createGroup(groupForm.getName(), groupForm.getDescription());
        response = ResponseEntity.ok().body(new ApiResponse<>(newGroup));

        return response;
    }

    @PatchMapping("/group")
    public ResponseEntity<ApiResponse<Group>> editGroup(@RequestBody GroupForm groupForm)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {

        ResponseEntity<ApiResponse<Group>> response = null;
        Group editedGroup = groupService.editGroup(groupForm.getId(), groupForm.getName(),
                groupForm.getDescription());
        response = ResponseEntity.ok().body(new ApiResponse<>(editedGroup));

        return response;
    }

    @GetMapping("/group/{id}")
    public ResponseEntity<ApiResponse<Group>> getGroup(@PathVariable("id") UUID id)
            throws InstanceNotFoundException {
        ResponseEntity<ApiResponse<Group>> response = null;
        Group group = groupService.getGroup(id);
        response = ResponseEntity.ok().body(new ApiResponse<>(group));
        return response;
    }

    @PostMapping("/group/request")
    public ResponseEntity<ApiResponse<Request>> createRequest(@RequestBody RequestForm requestForm)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        ResponseEntity<ApiResponse<Request>> response = null;
        try {
            Request request = groupService.createRequest(requestForm.getGroupId(), requestForm.getUsername());
            response = ResponseEntity.ok().body(new ApiResponse<>(request));
        } catch (UserAlreadyInvitedException e) {
            response = ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_INVITED_USER, e.getMessage()));
        } catch (UserAlreadyPresentException e) {
            response = ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_MEMBER_GROUP, e.getMessage()));
        }
        return response;
    }

    @DeleteMapping("/group/{groupId}/members/{username}")
    public ResponseEntity<ApiResponse<Void>> kickUser(@PathVariable("groupId") UUID groupId,
            @PathVariable("username") String username)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        ResponseEntity<ApiResponse<Void>> response = null;
        try {
            groupService.kickUser(groupId, username);
            response = ResponseEntity.ok().body(new ApiResponse<>());
        } catch (KickedCreatorException e) {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(null, e.getMessage()));
        } catch (UserKickedIsNotMember e) {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(CodeErrors.NOT_GROUP_MEMBER, e.getMessage()));
        }
        return response;
    }

    @PostMapping("/group/request/{requestId}")
    public ResponseEntity<ApiResponse<Void>> respondRequest(@PathVariable("requestId") UUID requestId,
            @RequestParam boolean accept)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        ResponseEntity<ApiResponse<Void>> response = null;
        groupService.respondRequest(requestId, accept);
        response = ResponseEntity.ok().body(new ApiResponse<>());
        return response;
    }

}