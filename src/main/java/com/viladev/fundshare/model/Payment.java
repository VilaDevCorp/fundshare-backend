package com.viladev.fundshare.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedEntityGraph(name = "Payment.userPayments", attributeNodes = {
        @NamedAttributeNode(value = "userPayments", subgraph = "userPayments"),
        @NamedAttributeNode("group"),
        @NamedAttributeNode("createdBy") }, subgraphs = @NamedSubgraph(name = "userPayments", attributeNodes = @NamedAttributeNode("user")))
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    private String description;

    // we need to fetch the userPayments eagerly as they are a fundamental part of
    // the payment
    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    Group group;

    @OneToMany(mappedBy = "payment", orphanRemoval = true, cascade = CascadeType.ALL)
    List<UserPayment> userPayments = new ArrayList<>();

    public Payment(String description, User createdBy, Group group) {
        this.description = description;
        this.createdBy = createdBy;
        this.group = group;
    }

}
