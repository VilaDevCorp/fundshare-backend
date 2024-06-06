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

    String description;

    GroupDto group;

    Double totalAmount;

    Set<UserPaymentDto> userPayments;

    public PaymentDto(Payment payment) {
        super(payment);
        this.description = payment.getDescription();
        this.group = payment.getGroup() != null ? new GroupDto(payment.getGroup()) : null;
        if (payment.getUserPayments() != null && !payment.getUserPayments().isEmpty()) {
            Set<UserPaymentDto> userPayments = new HashSet<>();
            Double totalAmount = 0.0;
            for (UserPayment userPayment : payment.getUserPayments()) {
                userPayments.add(new UserPaymentDto(userPayment, false));
                totalAmount += userPayment.getAmount();
            }
            this.totalAmount = totalAmount;
            this.userPayments = userPayments;
        }
    }

}
