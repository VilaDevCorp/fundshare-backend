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
    private String[] usernames;

    public RequestForm(UUID groupId, String[] usernames) {
        this.groupId = groupId;
        this.usernames = usernames;
    }

}
