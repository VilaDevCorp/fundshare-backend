package com.viladev.fundshare.model.dto;

import java.util.HashSet;
import java.util.Set;

import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.UserPayment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDto extends BaseEntityDto {

    GroupDto group;

    Set<UserPaymentDto> userPayments;

    public PaymentDto(Payment payment) {
        super(payment);
        this.group = payment.getGroup() != null ? new GroupDto(payment.getGroup()) : null;
        if (payment.getUserPayments() != null && !payment.getUserPayments().isEmpty()) {
            Set<UserPaymentDto> userPayments = new HashSet<>();
            for (UserPayment userPayment : payment.getUserPayments()) {
                userPayments.add(new UserPaymentDto(userPayment, false));
            }
            this.userPayments = userPayments;
        }
    }

}
