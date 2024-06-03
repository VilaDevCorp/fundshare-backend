package com.viladev.fundshare.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.viladev.fundshare.exceptions.InactiveGroupException;
import com.viladev.fundshare.exceptions.KickedCreatorException;
import com.viladev.fundshare.exceptions.NonZeroBalanceException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.UserAlreadyInvitedException;
import com.viladev.fundshare.exceptions.UserAlreadyPresentException;
import com.viladev.fundshare.exceptions.UserKickedIsNotMember;
import com.viladev.fundshare.forms.GroupForm;
import com.viladev.fundshare.forms.RequestForm;
import com.viladev.fundshare.forms.SearchGroupForm;
import com.viladev.fundshare.forms.SearchRequestForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.dto.GroupDto;
import com.viladev.fundshare.model.dto.PageDto;
import com.viladev.fundshare.model.dto.RequestDto;
import com.viladev.fundshare.service.GroupService;
import com.viladev.fundshare.service.UserService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;

@RestController
@RequestMapping("/api")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(UserService userService, GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/group")
    public ResponseEntity<ApiResponse<GroupDto>> createGroup(@RequestBody GroupForm groupForm)
            throws InstanceNotFoundException, EmptyFormFieldsException {
        Group newGroup = groupService.createGroup(groupForm.getName(), groupForm.getDescription());

        return ResponseEntity.ok().body(new ApiResponse<GroupDto>(new GroupDto(newGroup)));
    }

    @PatchMapping("/group/{id}")
    public ResponseEntity<ApiResponse<GroupDto>> editGroup(@PathVariable String id, @RequestBody GroupForm groupForm)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {

        Group editedGroup;
        try {
            UUID groupId = UUID.fromString(id);
            editedGroup = groupService.editGroup(groupId, groupForm.getName(),
                    groupForm.getDescription());
        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.CLOSED_GROUP, e.getMessage()));
        }

        return ResponseEntity.ok().body(new ApiResponse<GroupDto>(new GroupDto(editedGroup)));
    }

    @GetMapping("/group/{id}")
    public ResponseEntity<ApiResponse<GroupDto>> getGroup(@PathVariable("id") UUID id)
            throws InstanceNotFoundException {
        Group group = groupService.getGroupById(id);

        return ResponseEntity.ok().body(new ApiResponse<GroupDto>(new GroupDto(group)));
    }

    @PostMapping("/group/search")
    public ResponseEntity<ApiResponse<PageDto<GroupDto>>> searchGroups(@RequestBody SearchGroupForm searchForm) {
        PageDto<GroupDto> result = groupService.searchGroups(searchForm);
        return ResponseEntity.ok().body(new ApiResponse<>(result));

    }

    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable("groupId") UUID groupId)
            throws InstanceNotFoundException, NotAllowedResourceException {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().body(new ApiResponse<>());
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Set<RequestDto>>> createRequest(@RequestBody RequestForm requestForm)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        try {
            Set<Request> requests = groupService.createRequests(requestForm.getGroupId(), requestForm.getUsernames());
            Set<RequestDto> requestDtos = RequestDto.toSetRequestDto(requests);
            return ResponseEntity.ok().body(new ApiResponse<>(requestDtos));
        } catch (UserAlreadyInvitedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_INVITED_USER, e.getMessage()));
        } catch (UserAlreadyPresentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_MEMBER_GROUP, e.getMessage()));
        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.CLOSED_GROUP, e.getMessage()));
        }

    }

    @DeleteMapping("/group/{groupId}/members/{username}")
    public ResponseEntity<ApiResponse<Void>> kickUser(@PathVariable("groupId") UUID groupId,
            @PathVariable("username") String username)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        try {
            groupService.kickUser(groupId, username);

            return ResponseEntity.ok().body(new ApiResponse<>());
        } catch (KickedCreatorException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.KICKED_CREATOR, e.getMessage()));
        } catch (UserKickedIsNotMember e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(CodeErrors.NOT_GROUP_MEMBER, e.getMessage()));
        } catch (NonZeroBalanceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.NON_ZERO_BALANCE, e.getMessage()));
        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.CLOSED_GROUP, e.getMessage()));
        }
    }

    @PostMapping("/request/search")
    public ResponseEntity<ApiResponse<PageDto<RequestDto>>> getUserRequests(@RequestBody SearchRequestForm searchForm)
            throws InstanceNotFoundException, NotAllowedResourceException {
        PageDto<RequestDto> result = groupService.findRequestsOfUser(searchForm);
        return ResponseEntity.ok().body(new ApiResponse<>(result));
    }

    @PostMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<Void>> respondRequest(@PathVariable("requestId") UUID requestId,
            @RequestParam boolean accept)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        try {
            groupService.respondRequest(requestId, accept);
        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.CLOSED_GROUP, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>());
    }

    @PostMapping("/group/{groupId}/close")
    public ResponseEntity<ApiResponse<Void>> closeGroup(@PathVariable("groupId") UUID groupId)
            throws InstanceNotFoundException, NotAllowedResourceException {
        groupService.closeGroup(groupId);
        return ResponseEntity.ok().body(new ApiResponse<>());
    }
}