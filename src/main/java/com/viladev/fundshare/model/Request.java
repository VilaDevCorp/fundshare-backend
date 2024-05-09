package com.viladev.fundshare.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
public class Request extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    User user;

}
