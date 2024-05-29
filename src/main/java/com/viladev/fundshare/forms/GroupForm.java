package com.viladev.fundshare.forms;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GroupForm {

    private UUID id;
    private String name;
    private String description;

    public GroupForm(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
