package com.viladev.fundshare.forms;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GroupUserForm {

    private String username;
    private UUID groupId;

    public GroupUserForm(String username, UUID groupId) {
        this.username = username;
        this.groupId = groupId;
    }

}
