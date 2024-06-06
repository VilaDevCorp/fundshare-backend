package com.viladev.fundshare.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DebtDto {

    UserDto payer;
    UserDto payee;
    Double amount;

    public DebtDto(UserDto payer, UserDto payee, Double amount) {
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
    }

}
