package com.viladev.fundshare.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class UserPaymentId implements Serializable {

    UUID userId;
    UUID paymentId;

    public UserPaymentId(UUID userId, UUID paymentId) {
        this.userId = userId;
        this.paymentId = paymentId;
    }
}
