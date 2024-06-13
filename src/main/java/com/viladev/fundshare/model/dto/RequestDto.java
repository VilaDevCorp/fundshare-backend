package com.viladev.fundshare.model.dto;

import java.util.HashSet;
import java.util.Set;

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

    public static Set<RequestDto> toSetRequestDto(Set<Request> requests) {
        Set<RequestDto> requestsDto = new HashSet<>();
        requests.forEach(request -> requestsDto.add(new RequestDto(request)));
        return requestsDto;
    }

    GroupDto group;

    UserDto user;

}
