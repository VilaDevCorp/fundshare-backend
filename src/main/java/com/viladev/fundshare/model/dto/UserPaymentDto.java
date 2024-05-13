package com.viladev.fundshare.model.dto;

import com.viladev.fundshare.model.UserPayment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPaymentDto {

    UserDto user;
    PaymentDto payment;
    Double amount;

    public UserPaymentDto(UserPayment userPayment, boolean includePayment) {
        this.user = userPayment.getUser() != null ? new UserDto(userPayment.getUser()) : null;
        this.payment = userPayment.getPayment() != null && includePayment ? new PaymentDto(userPayment.getPayment())
                : null;
        this.amount = userPayment.getAmount();
    }

}
