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
import com.viladev.fundshare.utils.FilterUtils;

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
            throws EmptyFormFieldsException, NotAbove0AmountException, InstanceNotFoundException {
        if (paymentForm.getPayees() == null || paymentForm.getPayees().isEmpty()) {
            throw new EmptyFormFieldsException();
        }
        User creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        Group group = paymentForm.getGroupId() != null ? groupRepository.findById(paymentForm.getGroupId())
                .orElseThrow(() -> new InstanceNotFoundException("Group not found")) : null;

        Payment payment = new Payment(creator, group);
        payment = paymentRepository.save(payment);

        for (UserPaymentForm userPaymentForm : paymentForm.getPayees()) {
            User user = userRepository.findByUsername(userPaymentForm.getUsername());
            if (user == null) {
                throw new InstanceNotFoundException("User not found");
            }
            if (userPaymentForm.getAmount() <= 0) {
                throw new NotAbove0AmountException();
            }
            UserPayment userPayment = new UserPayment(user, payment, userPaymentForm.getAmount());
            userPaymentRepository.save(userPayment);
        }
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
        FilterUtils.checkIfCreator(payment);
        paymentRepository.delete(payment);
    }

}
