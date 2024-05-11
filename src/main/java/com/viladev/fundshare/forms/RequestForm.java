package com.viladev.fundshare.forms;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RequestForm {

    private UUID groupId;
    private String username;

    public RequestForm(UUID groupId, String username) {
        this.groupId = groupId;
        this.username = username;
    }

}
