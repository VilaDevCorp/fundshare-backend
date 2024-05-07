package com.template.backtemplate.controller;

import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.template.backtemplate.auth.AuthResult;
import com.template.backtemplate.exceptions.AlreadyUsedValidationCodeException;
import com.template.backtemplate.exceptions.EmailAlreadyInUseException;
import com.template.backtemplate.exceptions.EmptyFormFieldsException;
import com.template.backtemplate.exceptions.ExpiredValidationCodeException;
import com.template.backtemplate.exceptions.IncorrectValidationCodeException;
import com.template.backtemplate.exceptions.InvalidCredentialsException;
import com.template.backtemplate.exceptions.NotValidatedAccountException;
import com.template.backtemplate.exceptions.UsernameAlreadyInUseException;
import com.template.backtemplate.forms.LoginForm;
import com.template.backtemplate.forms.RegisterForm;
import com.template.backtemplate.model.User;
import com.template.backtemplate.service.UserService;
import com.template.backtemplate.utils.ApiResponse;
import com.template.backtemplate.utils.CodeErrors;
import com.template.backtemplate.utils.ValidationCodeTypeEnum;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/public")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Value("${auth.cookie.domain}")
    private String cookieDomain;

    @Value("${auth.cookie.path}")
    private String cookiePath;

    @Value("${auth.cookie.secure}")
    private boolean cookieSecure;

    @Value("${auth.cookie.httpOnly}")
    private boolean cookieHttpOnly;

    @Value("${auth.cookie.sameSite}")
    private String cookieSameSite;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok().body("API IS UP AND RUNNING");
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody RegisterForm registerForm) {
        ResponseEntity<ApiResponse<User>> response = null;
        try {
            User newUser = userService.registerUser(registerForm.getEmail(), registerForm.getUsername(),
                    registerForm.getPassword());
            response = ResponseEntity.ok().body(new ApiResponse<>(newUser));

        } catch (EmailAlreadyInUseException e) {
            response = ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.EMAIL_ALREADY_IN_USE, e.getMessage()));

        } catch (UsernameAlreadyInUseException e) {
            response = ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.USERNAME_ALREADY_IN_USE, e.getMessage()));

        } catch (EmptyFormFieldsException e) {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, e.getMessage()));

        } catch (Exception e) {
            response = ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginForm loginForm, HttpServletResponse response) {
        String csrfToken = UUID.randomUUID().toString();
        AuthResult authResult = null;
        try {
            authResult = userService.authenticate(loginForm.getUsername(), loginForm.getPassword());
            if (authResult == null) {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponse<>(null, "An error occurred while trying to authenticate"));
            }

        } catch (EmptyFormFieldsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, e.getMessage()));

        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(CodeErrors.INVALID_CREDENTIALS, e.getMessage()));

        } catch (NotValidatedAccountException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.NOT_VALIDATED_ACCOUNT, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
        Cookie cookie = new Cookie("JWT_TOKEN", authResult.getJwtToken());
        cookie.setDomain(cookieDomain);
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
        return ResponseEntity.ok().body(new ApiResponse<>(csrfToken));
    }

    @PostMapping("/validate/{username}/{validationCode}")
    public ResponseEntity<ApiResponse<Boolean>> validateAccount(@PathVariable String username,
            @PathVariable String validationCode) {
        if (username == null || validationCode == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        try {
            userService.activateAccount(username, validationCode);
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, e.getMessage()));
        } catch (ExpiredValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new ApiResponse<>(CodeErrors.EXPIRED_VALIDATION_CODE, e.getMessage()));
        } catch (AlreadyUsedValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_USED_VALIDATION_CODE, e.getMessage()));
        } catch (IncorrectValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(CodeErrors.INCORRECT_VALIDATION_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>(true));
    }

    @PostMapping("/validate/{username}/resend")
    public ResponseEntity<ApiResponse<Boolean>> resendValidationCode(@PathVariable String username) {
        if (username == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        try {
            userService.createValidationCode(username, ValidationCodeTypeEnum.ACTIVATE_ACCOUNT);
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>(true));
    }

    @PostMapping("/forgottenpassword/{username}")
    public ResponseEntity<ApiResponse<Boolean>> sendResetPasswordCode(@PathVariable String username) {
        if (username == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, "There are mandatory fields that are empty"));
        }
        try {
            userService.createValidationCode(username, ValidationCodeTypeEnum.RESET_PASSWORD);
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>(true));
    }

    @PostMapping("/resetpassword/{username}/{validationCode}")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@PathVariable String username,
            @PathVariable String validationCode, @RequestBody String newPassword) {
        if (username == null || validationCode == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, "There are mandatory fields that are empty"));
        }
        try {
            userService.resetPassword(username, validationCode, newPassword);
        } catch (InstanceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, e.getMessage()));
        } catch (ExpiredValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new ApiResponse<>(CodeErrors.EXPIRED_VALIDATION_CODE, e.getMessage()));
        } catch (AlreadyUsedValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_USED_VALIDATION_CODE, e.getMessage()));
        } catch (IncorrectValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(CodeErrors.INCORRECT_VALIDATION_CODE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>(true));
    }
}