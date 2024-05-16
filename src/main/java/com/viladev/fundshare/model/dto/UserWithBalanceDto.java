package com.viladev.fundshare.model.dto;

import com.viladev.fundshare.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserWithBalanceDto extends BaseEntityDto {

    String email;

    String username;

    Double balance = 0.0;

    public UserWithBalanceDto(User user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.balance = user.getBalance();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserWithBalanceDto user = (UserWithBalanceDto) obj;
        return username.equals(user.username);
    }

}
