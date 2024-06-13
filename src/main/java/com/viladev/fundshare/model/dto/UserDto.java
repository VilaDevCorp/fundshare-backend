package com.viladev.fundshare.model.dto;

import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.UserConf;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto extends BaseEntityDto {

    String email;

    String username;

    UserConf conf = new UserConf();

    public UserDto(User user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.conf = user.getConf();
    }

    public static UserDto[] toUserDtoArray(User[] users) {
        UserDto[] userDtos = new UserDto[users.length];
        for (int i = 0; i < users.length; i++) {
            userDtos[i] = new UserDto(users[i]);
        }
        return userDtos;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserDto user = (UserDto) obj;
        return username.equals(user.username);
    }

}
