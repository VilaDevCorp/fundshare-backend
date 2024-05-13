package com.viladev.fundshare.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    UserPaymentId id = new UserPaymentId();

    @ManyToOne (optional = false)
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // we don't need to fetch the payment data as usually this
                                                         // entity is fetched through the payment
    @JoinColumn(name = "payment_id")
    @MapsId("paymentId")
    Payment payment;

    @NotNull
    Double amount;

    public UserPayment(User user, Payment payment, Double amount) {
        this.user = user;
        this.payment = payment;
        this.amount = amount;
    }

}
