package com.viladev.fundshare.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserPaymentForm {

    private String username;
    private Double amount;

    public UserPaymentForm(String username, Double amount) {
        this.username = username;
        this.amount = amount;
    }

}
