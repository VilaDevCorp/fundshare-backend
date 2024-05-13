package com.viladev.fundshare.model.dto;

import com.viladev.fundshare.model.Request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestDto extends BaseEntityDto {

    public RequestDto(Request request) {
        super(request);
        this.group = request.getGroup() != null ? new GroupDto(request.getGroup()) : null;
        this.user = request.getUser() != null ? new UserDto(request.getUser()) : null;
    }

    GroupDto group;

    UserDto user;

}
