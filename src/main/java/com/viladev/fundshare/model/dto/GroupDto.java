package com.viladev.fundshare.model.dto;

import com.viladev.fundshare.model.Group;

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

    public GroupDto(Group group) {
        super(group);
        this.name = group.getName();
        this.description = group.getDescription();
        this.active = group.isActive();
    }
}
