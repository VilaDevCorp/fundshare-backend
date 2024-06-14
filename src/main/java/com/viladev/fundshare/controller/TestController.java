package com.viladev.fundshare.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladev.fundshare.model.User;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.GroupUserRepository;
import com.viladev.fundshare.repository.PaymentRepository;
import com.viladev.fundshare.repository.RequestRepository;
import com.viladev.fundshare.repository.UserPaymentRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.repository.ValidationCodeRepository;

@RestController
@RequestMapping("/api")
@Profile("e2e")
public class TestController {

    @Autowired
    private ValidationCodeRepository validationCodeRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private GroupUserRepository groupUserRepository;
    @Autowired
    private UserPaymentRepository userPaymentRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;


    @DeleteMapping("/public/test/database")
    public void cleanUpDatabase() {
        validationCodeRepository.deleteAll();
        requestRepository.deleteAll();
        groupUserRepository.deleteAll();
        userPaymentRepository.deleteAll();
        paymentRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @PostMapping("/public/test/populate")
    public void populateDatabase() {
        // Create users
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("1234");
        User user = new User("e2e@gmail.com", "e2e", encodedPassword);
        user.setValidated(true);
        userRepository.save(user);
    }
}