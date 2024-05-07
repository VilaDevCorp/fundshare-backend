package com.template.backtemplate.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RegisterForm {

    private String email;
    private String username;
    private String password;

    public RegisterForm(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

}
