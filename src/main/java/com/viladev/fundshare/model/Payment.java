package com.viladev.fundshare.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    User payer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    Group group;


    @NotNull
    Double totalAmount;

}
