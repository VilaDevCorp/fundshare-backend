package com.viladev.fundshare.controller;

import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.forms.GroupForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.service.GroupService;
import com.viladev.fundshare.service.UserService;
import com.viladev.fundshare.utils.ApiResponse;

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
            throws InstanceNotFoundException {
        ResponseEntity<ApiResponse<Group>> response = null;
        try {
            User currentUser = userService
                    .getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            Group newGroup = groupService.createGroup(groupForm.getName(), groupForm.getDescription(), currentUser);
            response = ResponseEntity.ok().body(new ApiResponse<>(newGroup));

        } catch (EmptyFormFieldsException e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, e.getMessage()));

        }
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
}