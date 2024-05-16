package com.viladev.fundshare.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedEntityGraph(name = "Group.users", attributeNodes = { @NamedAttributeNode("users"),
        @NamedAttributeNode("createdBy") })
@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
public class Group extends BaseEntity {

    @NotNull
    String name;

    String description;

    boolean active = true;

    @ManyToMany
    @JoinTable(name = "users_groups", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Request> requests = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new HashSet<>();

    public Group(String name, String description, User createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

}
