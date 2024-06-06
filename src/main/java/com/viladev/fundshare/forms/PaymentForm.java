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

    private String description;
    private UUID groupId;
    private Set<UserPaymentForm> payees;

    public PaymentForm(String description, UUID groupId, Set<UserPaymentForm> payees) {
        this.description = description;
        this.groupId = groupId;
        this.payees = payees;
    }

}
