package com.viladev.fundshare.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.GroupUser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupDto extends BaseEntityDto {

    String name;
    String description;
    boolean active = true;
    Double balance;
    List<UserDto> users;

    public GroupDto(Group group) {
        super(group);
        this.name = group.getName();
        this.description = group.getDescription();
        this.active = group.isActive();
        try {
            if (group.getGroupUsers() != null) {
                java.util.List<UserDto> groupUsers = new ArrayList<>();
                for (GroupUser gu : group.getGroupUsers()) {
                    groupUsers.add(new UserDto(gu.getUser()));
                }
                this.users = groupUsers;
            }
        } catch (Exception e) {
            this.users = null;
        }
    }

    public GroupDto(Group group, Double balance) {
        this(group);
        this.balance = balance;
    }
}
