package com.viladev.fundshare.service;

import java.util.Set;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladev.fundshare.exceptions.NotAbove0AmountException;
import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.PayeeIsNotInGroupException;
import com.viladev.fundshare.exceptions.PayerIsNotInGroupException;
import com.viladev.fundshare.forms.PaymentForm;
import com.viladev.fundshare.forms.UserPaymentForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.UserPayment;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.PaymentRepository;
import com.viladev.fundshare.repository.UserPaymentRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.utils.AuthUtils;

@Service
@Transactional
public class PaymentService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final PaymentRepository paymentRepository;

    private final UserPaymentRepository userPaymentRepository;

    @Autowired
    public PaymentService(GroupRepository groupRepository, UserRepository userRepository,
            PaymentRepository paymentRepository,
            UserPaymentRepository userPaymentRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.userPaymentRepository = userPaymentRepository;
    }

    public Payment createPayment(PaymentForm paymentForm)
            throws EmptyFormFieldsException, NotAbove0AmountException, InstanceNotFoundException,
            PayerIsNotInGroupException,
            PayeeIsNotInGroupException {
        if (paymentForm.getPayees() == null || paymentForm.getPayees().isEmpty() || paymentForm.getGroupId() == null) {
            throw new EmptyFormFieldsException();
        }
        User creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        Group group = groupRepository.findById(paymentForm.getGroupId())
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));

        if (!group.getUsers().contains(creator)) {
            throw new PayerIsNotInGroupException();
        }

        Payment payment = new Payment(creator, group);
        payment = paymentRepository.save(payment);

        Double totalAmount = 0.0;
        for (UserPaymentForm userPaymentForm : paymentForm.getPayees()) {
            User user = userRepository.findByUsername(userPaymentForm.getUsername());
            if (user == null) {
                throw new InstanceNotFoundException("User not found");
            }
            if (!group.getUsers().contains(user)) {
                throw new PayeeIsNotInGroupException();
            }

            if (userPaymentForm.getAmount() <= 0) {
                throw new NotAbove0AmountException();
            }
            UserPayment userPayment = new UserPayment(user, payment, userPaymentForm.getAmount());
            userPaymentRepository.save(userPayment);
            user.setBalance(user.getBalance() + userPaymentForm.getAmount());
            userRepository.save(user);
            totalAmount += userPaymentForm.getAmount();
        }

        User creatorUser = userRepository.findByUsername(creator.getUsername());
        creatorUser.setBalance(creatorUser.getBalance() - totalAmount);
        userRepository.save(creatorUser);
        payment = paymentRepository.getReferenceById(payment.getId());
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(UUID id) throws InstanceNotFoundException {
        return paymentRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
    }

    public void deletePayment(UUID id) throws InstanceNotFoundException,
            NotAllowedResourceException {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        AuthUtils.checkIfCreator(payment);
        Double totalAmount = 0.0;
        for (UserPayment userPayment : payment.getUserPayments()) {
            User user = userRepository.findByUsername(userPayment.getUser().getUsername());
            if (user == null) {
                continue;
            }
            user.setBalance(user.getBalance() - userPayment.getAmount());
            userRepository.save(user);
            totalAmount += userPayment.getAmount();
        }
        User creatorUser = userRepository.findByUsername(payment.getCreatedBy().getUsername());
        creatorUser.setBalance(creatorUser.getBalance() + totalAmount);
        userRepository.save(creatorUser);

        paymentRepository.delete(payment);
    }

}
