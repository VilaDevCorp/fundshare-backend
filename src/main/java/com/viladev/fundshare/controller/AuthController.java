package com.viladev.fundshare.controller;

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
import com.viladev.fundshare.auth.AuthResult;
import com.viladev.fundshare.exceptions.AlreadyUsedValidationCodeException;
import com.viladev.fundshare.exceptions.EmailAlreadyInUseException;
import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.ExpiredValidationCodeException;
import com.viladev.fundshare.exceptions.IncorrectValidationCodeException;
import com.viladev.fundshare.exceptions.InvalidCredentialsException;
import com.viladev.fundshare.exceptions.NotValidatedAccountException;
import com.viladev.fundshare.exceptions.SendEmailException;
import com.viladev.fundshare.exceptions.UsernameAlreadyInUseException;
import com.viladev.fundshare.forms.LoginForm;
import com.viladev.fundshare.forms.RegisterForm;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.dto.UserDto;
import com.viladev.fundshare.service.UserService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;
import com.viladev.fundshare.utils.ValidationCodeTypeEnum;

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
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@RequestBody RegisterForm registerForm)
            throws InstanceNotFoundException, SendEmailException, EmptyFormFieldsException {
        try {
            User newUser = userService.registerUser(registerForm.getEmail(), registerForm.getUsername(),
                    registerForm.getPassword());
            return ResponseEntity.ok().body(new ApiResponse<>(new UserDto(newUser)));

        } catch (EmailAlreadyInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.EMAIL_ALREADY_IN_USE, e.getMessage()));

        } catch (UsernameAlreadyInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.USERNAME_ALREADY_IN_USE, e.getMessage()));

        }

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginForm loginForm, HttpServletResponse response)
            throws EmptyFormFieldsException {
        AuthResult authResult = null;
        try {
            authResult = userService.authenticate(loginForm.getUsername(), loginForm.getPassword());
            if (authResult == null) {
                throw new InternalError();
            }

        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(CodeErrors.INVALID_CREDENTIALS, e.getMessage()));

        } catch (NotValidatedAccountException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.NOT_VALIDATED_ACCOUNT, e.getMessage()));
        }
        Cookie cookie = new Cookie("JWT_TOKEN", authResult.getJwtToken());
        cookie.setDomain(cookieDomain);
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
        return ResponseEntity.ok().body(new ApiResponse<>(authResult.getCsrfToken().toString()));
    }

    @PostMapping("/validate/{username}/{validationCode}")
    public ResponseEntity<ApiResponse<Boolean>> validateAccount(@PathVariable String username,
            @PathVariable String validationCode) throws InstanceNotFoundException, EmptyFormFieldsException {
        if (username == null || validationCode == null) {
            throw new EmptyFormFieldsException();
        }
        try {
            userService.activateAccount(username, validationCode);
        } catch (ExpiredValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new ApiResponse<>(CodeErrors.EXPIRED_VALIDATION_CODE, e.getMessage()));
        } catch (AlreadyUsedValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_USED_VALIDATION_CODE, e.getMessage()));
        } catch (IncorrectValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(CodeErrors.INCORRECT_VALIDATION_CODE, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>(null));
    }

    @PostMapping("/validate/{username}/resend")
    public ResponseEntity<ApiResponse<Boolean>> resendValidationCode(@PathVariable String username)
            throws EmptyFormFieldsException, InstanceNotFoundException, SendEmailException {
        if (username == null) {
            throw new EmptyFormFieldsException();
        }
        userService.createValidationCode(username, ValidationCodeTypeEnum.ACTIVATE_ACCOUNT);

        return ResponseEntity.ok().body(new ApiResponse<>(null));
    }

    @PostMapping("/forgottenpassword/{username}")
    public ResponseEntity<ApiResponse<Boolean>> sendResetPasswordCode(@PathVariable String username)
            throws InstanceNotFoundException, SendEmailException, EmptyFormFieldsException {
        if (username == null) {
            throw new EmptyFormFieldsException();
        }
        userService.createValidationCode(username, ValidationCodeTypeEnum.RESET_PASSWORD);
        return ResponseEntity.ok().body(new ApiResponse<>(null));
    }

    @PostMapping("/resetpassword/{username}/{validationCode}")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@PathVariable String username,
            @PathVariable String validationCode, @RequestBody String newPassword)
            throws InstanceNotFoundException, EmptyFormFieldsException {
        if (username == null || validationCode == null) {
            throw new EmptyFormFieldsException();
        }
        try {
            userService.resetPassword(username, validationCode, newPassword);
        } catch (ExpiredValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new ApiResponse<>(CodeErrors.EXPIRED_VALIDATION_CODE, e.getMessage()));
        } catch (AlreadyUsedValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(CodeErrors.ALREADY_USED_VALIDATION_CODE, e.getMessage()));
        } catch (IncorrectValidationCodeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(CodeErrors.INCORRECT_VALIDATION_CODE, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<>(null));
    }
}