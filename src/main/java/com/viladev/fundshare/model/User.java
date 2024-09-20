package com.viladev.fundshare.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(unique = true)
    @NotNull
    String email;

    @Column(unique = true)
    @NotNull
    @Length(min = 1, max = 80)
    String username;

    @NotNull
    @JsonIgnore
    String password;

    @JsonIgnore
    boolean validated = false;

    Double balance = 0.0;

    @Convert(converter = UserConfConverter.class)
    UserConf conf = new UserConf();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ValidationCode> validationCodes = new HashSet<>();

    @Transient
    private String pictureUrl;

    @Version
    private Long version;

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return username.equals(user.username);
    }

    public int hashCode() {
        return username.hashCode();
    }

}
