package com.viladev.fundshare.model;

import java.util.Calendar;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_users")
@Getter
@Setter
@NoArgsConstructor
public class GroupUser {

    @EmbeddedId
    GroupUserId id = new GroupUserId();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // we don't need to fetch the group data as usually this
                                 // entity is fetched through the group
    @JoinColumn(name = "group_id")
    @MapsId("groupId")
    Group group;

    @NotNull
    Calendar joinDate = Calendar.getInstance();

    public GroupUser(User user, Group group) {
        this.user = user;
        this.group = group;
    }

}
