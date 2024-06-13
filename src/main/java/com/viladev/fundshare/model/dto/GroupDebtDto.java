package com.viladev.fundshare.model.dto;

import com.viladev.fundshare.model.Group;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupDebtDto {

    GroupDto group;
    Double amount;

    public GroupDebtDto(GroupDto group, Double amount) {
        this.group = group;
        this.amount = amount;
    }

}
