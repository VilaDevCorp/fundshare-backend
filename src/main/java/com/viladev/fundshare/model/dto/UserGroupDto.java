package com.viladev.fundshare.model.dto;

import java.util.Calendar;

import com.viladev.fundshare.model.GroupUser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserGroupDto {

    UserDto user;
    GroupDto group;
    Calendar joinDate;

    public UserGroupDto(GroupUser groupUser) {
        this.user = groupUser.getUser() != null ? new UserDto(groupUser.getUser()) : null;
        this.group = groupUser.getGroup() != null ? new GroupDto(groupUser.getGroup()) : null;
        this.joinDate = groupUser.getJoinDate();
    }

}
