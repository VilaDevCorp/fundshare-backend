package com.viladev.fundshare.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_payments")
@Getter
@Setter
@NoArgsConstructor
public class UserPayment {

    @EmbeddedId
    UserPaymentId id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    User user;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @MapsId("paymentId")
    Payment payment;

    @NotNull
    Double amount;

}
