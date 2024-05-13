package com.viladev.fundshare.forms;

import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PaymentForm {

    private UUID groupId;
    private Set<UserPaymentForm> payees;

    public PaymentForm(UUID groupId, Set<UserPaymentForm> payees) {
        this.groupId = groupId;
        this.payees = payees;
    }

}
