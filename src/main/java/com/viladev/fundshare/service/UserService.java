package com.viladev.fundshare.service;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.viladev.fundshare.auth.AuthResult;
import com.viladev.fundshare.auth.JwtUtils;
import com.viladev.fundshare.exceptions.EmailAlreadyInUseException;
import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.ExpiredValidationCodeException;
import com.viladev.fundshare.exceptions.IncorrectValidationCodeException;
import com.viladev.fundshare.exceptions.AlreadyUsedValidationCodeException;
import com.viladev.fundshare.exceptions.InvalidCredentialsException;
import com.viladev.fundshare.exceptions.NotValidatedAccountException;
import com.viladev.fundshare.exceptions.SendEmailException;
import com.viladev.fundshare.exceptions.UserAlreadyValidatedException;
import com.viladev.fundshare.exceptions.UsernameAlreadyInUseException;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.ValidationCode;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.repository.ValidationCodeRepository;
import com.viladev.fundshare.utils.ValidationCodeTypeEnum;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final AuthenticationManager authenticationManager;

    private final ValidationCodeRepository validationCodeRepository;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final JwtUtils jwtUtils;

    @Autowired
    public UserService(AuthenticationManager authenticationManager, ValidationCodeRepository validationCodeRepository,
            UserRepository userRepository, EmailService emailService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.validationCodeRepository = validationCodeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
    }

    @Value("${mail.subject.account-activation}")
    private String accountActivationSubject;
    @Value("${mail.message.account-activation}")
    private String accountActivationMessage;
    @Value("${mail.subject.password-reset}")
    private String passwordResetSubject;
    @Value("${mail.message.password-reset}")
    private String passwordResetMessage;
    @Value("${frontend.url}")
    private String frontendUrl;

    public User registerUser(String email, String username, String password)
            throws UsernameAlreadyInUseException, EmailAlreadyInUseException, EmptyFormFieldsException,
            InstanceNotFoundException, SendEmailException, UserAlreadyValidatedException {
        if (userRepository.findByUsername(username) != null) {
            throw new UsernameAlreadyInUseException("An user is already using this username");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new EmailAlreadyInUseException("An user is already using this email");
        }
        if (username == null || password == null || email == null) {
            throw new EmptyFormFieldsException();
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password);
        User user = userRepository.save(new User(email, username, encodedPassword));
        createValidationCode(user.getUsername(),
                ValidationCodeTypeEnum.ACTIVATE_ACCOUNT);

        return user;
    }

    @Transactional(readOnly = true)
    public AuthResult authenticate(String username, String password)
            throws InvalidCredentialsException, EmptyFormFieldsException, NotValidatedAccountException {
        if (username == null || password == null) {
            throw new EmptyFormFieldsException();
        }
        User user = userRepository.findByUsername(username);
        // We return invalid credentials and not user not found to avoid user
        // enumeration
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticationResponse = null;
        try {
            authenticationResponse = authenticationManager.authenticate(authenticationRequest);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if (!user.isValidated()) {
            throw new NotValidatedAccountException("Account not validated yet");
        }
        UUID csrfToken = UUID.randomUUID();
        String jwtToken = jwtUtils.generateToken(authenticationResponse.getName(), csrfToken);

        return new AuthResult(jwtToken, csrfToken);
    }

    public ValidationCode createValidationCode(String username, ValidationCodeTypeEnum type)
            throws InstanceNotFoundException, SendEmailException, UserAlreadyValidatedException,
            EmptyFormFieldsException {
        if (username == null) {
            throw new EmptyFormFieldsException();
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException();
        }
        if (type.equals(ValidationCodeTypeEnum.ACTIVATE_ACCOUNT)) {
            if (user.isValidated()) {
                throw new UserAlreadyValidatedException(username + " is already validated");
            }
        }
        ValidationCode validationCode = new ValidationCode(type);
        validationCode.setCreatedBy(user);
        validationCode.setUser(user);
        validationCodeRepository.save(validationCode);

        try {
            if (type.equals(ValidationCodeTypeEnum.ACTIVATE_ACCOUNT)) {
                emailService.sendSimpleMessage(user.getEmail(), accountActivationSubject,
                        accountActivationMessage + "\n\n"
                                + frontendUrl + "/validate/" + username + "/" + validationCode.getCode());
            } else if (type.equals(ValidationCodeTypeEnum.RESET_PASSWORD)) {
                emailService.sendSimpleMessage(user.getEmail(), passwordResetSubject,
                        passwordResetMessage + "\n\n"
                                + frontendUrl + "/reset-password/" + username + "/" + validationCode.getCode());
            }
        } catch (Exception e) {
            throw new SendEmailException("Error sending validation email");
        }
        return validationCode;
    }

    private void validateCode(String username, ValidationCodeTypeEnum type, String code)
            throws InstanceNotFoundException, ExpiredValidationCodeException, AlreadyUsedValidationCodeException,
            IncorrectValidationCodeException {

        List<ValidationCode> validationCodeList = validationCodeRepository
                .findByUserUsernameAndTypeOrderByCreatedAtDesc(username, type.getType());
        if (validationCodeList.isEmpty()) {
            throw new InstanceNotFoundException();
        }
        ValidationCode lastValidationCode = validationCodeList.get(0);
        if (lastValidationCode.getCode().equals(code) && lastValidationCode.isUsed()) {
            throw new AlreadyUsedValidationCodeException("Validation code was already used");
        }
        Calendar expirationDate = (Calendar) lastValidationCode.getCreatedAt().clone();
        expirationDate.add(Calendar.MINUTE, ValidationCode.EXPIRATION_MINUTES);
        if (lastValidationCode.getCode().equals(code) && !expirationDate.after(Calendar.getInstance())) {
            throw new ExpiredValidationCodeException("Validation code expired");
        }
        if (!lastValidationCode.getCode().equals(code)) {
            throw new IncorrectValidationCodeException("Incorrect validation code");
        }
        lastValidationCode.setUsed(true);
    }

    public void activateAccount(String username, String code)
            throws InstanceNotFoundException, ExpiredValidationCodeException,
            AlreadyUsedValidationCodeException, IncorrectValidationCodeException, EmptyFormFieldsException {
        if (username == null || code == null) {
            throw new EmptyFormFieldsException();
        }
        validateCode(username, ValidationCodeTypeEnum.ACTIVATE_ACCOUNT, code);
        User user = userRepository.findByUsername(username);
        user.setValidated(true);
        userRepository.save(user);
    }

    public void resetPassword(String username, String code, String newPassword)
            throws InstanceNotFoundException, ExpiredValidationCodeException,
            AlreadyUsedValidationCodeException, IncorrectValidationCodeException, EmptyFormFieldsException {
        if (username == null || code == null) {
            throw new EmptyFormFieldsException();
        }

        validateCode(username, ValidationCodeTypeEnum.RESET_PASSWORD, code);
        User user = userRepository.findByUsername(username);
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
    }
}
