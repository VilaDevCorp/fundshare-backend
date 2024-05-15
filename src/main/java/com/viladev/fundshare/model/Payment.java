package com.viladev.fundshare.model;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    // we need to fetch the userPayments eagerly as they are a fundamental part of
    // the payment
    @ManyToOne(optional = true)
    @JoinColumn(name = "group_id")
    Group group;

    @OneToMany(mappedBy = "payment", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    Set<UserPayment> userPayments = new HashSet<>();

    public Payment(User createdBy, Group group) {
        this.createdBy = createdBy;
        this.group = group;
    }

}
