package com.viladev.fundshare.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class GroupUserId implements Serializable {

    UUID userId;
    UUID groupId;

    public GroupUserId(UUID userId, UUID groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}
