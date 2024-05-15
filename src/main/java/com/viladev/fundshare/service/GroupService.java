package com.viladev.fundshare.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.KickedCreatorException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.UserAlreadyInvitedException;
import com.viladev.fundshare.exceptions.UserAlreadyPresentException;
import com.viladev.fundshare.exceptions.UserKickedIsNotMember;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.PaymentRepository;
import com.viladev.fundshare.repository.RequestRepository;
import com.viladev.fundshare.repository.UserPaymentRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.utils.FilterUtils;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final RequestRepository requestRepository;

    private final PaymentRepository paymentRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
            RequestRepository requestRepository, PaymentRepository paymentRepository) {

        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.paymentRepository = paymentRepository;
    }

    public Group createGroup(String name, String description) throws EmptyFormFieldsException {
        User creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (name == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = new Group(name, description, creator);
        group.setUsers(Set.of(creator));
        return groupRepository.save(group);
    }

    public Group editGroup(UUID id, String name, String description)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {
        if (id == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        FilterUtils.checkIfCreator(group);

        if (name != null) {
            group.setName(name);
        }
        if (description != null) {
            group.setDescription(description);
        }
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public Group getGroupById(UUID id) throws InstanceNotFoundException {
        return groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
    }

    public void deleteGroup(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Group group = groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        FilterUtils.checkIfCreator(group);

        Map<UUID, Double> userBalances = new HashMap<>();

        Set<Payment> payments = paymentRepository.findByGroupId(id);

        // we loop through all the payments of the group
        payments.stream().forEach(payment -> {
            // We use this variable to store the total amount of the payment to add it to
            // the payer's balance
            AtomicReference<Double> totalAmount = new AtomicReference<>(0.0);

            // we loop through all the userPayments of the payment updating the payee's balances
            payment.getUserPayments().stream().forEach(userPayment -> {
                totalAmount.updateAndGet(value -> value + userPayment.getAmount());
                if (userBalances.containsKey(userPayment.getUser().getId())) {
                    userBalances.put(userPayment.getUser().getId(),
                            userBalances.get(userPayment.getUser().getId()) - userPayment.getAmount());
                } else {
                    userBalances.put(userPayment.getUser().getId(), -userPayment.getAmount());
                }
            });
            if (userBalances.containsKey(payment.getCreatedBy().getId())) {
                userBalances.put(payment.getCreatedBy().getId(),
                        userBalances.get(payment.getCreatedBy().getId()) + totalAmount.get());
            } else {
                userBalances.put(payment.getCreatedBy().getId(), totalAmount.get());
            }
        });

        userBalances.entrySet().stream().forEach(entry -> {
            User user = userRepository.findById(entry.getKey()).get();
            user.setBalance(user.getBalance() + entry.getValue());
            userRepository.save(user);
        });

        groupRepository.delete(group);
    }

    public Request createRequest(UUID groupId, String username)
            throws InstanceNotFoundException, NotAllowedResourceException, UserAlreadyPresentException,
            UserAlreadyInvitedException, EmptyFormFieldsException {
        if (groupId == null || username == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException("User not found");
        }
        if (group.getUsers().contains(user)) {
            throw new UserAlreadyPresentException("User already present in the group");
        }
        if (requestRepository.findByGroupIdAndUserId(groupId, user.getId()) != null) {
            throw new UserAlreadyInvitedException("User already invited to the group");
        }
        FilterUtils.checkIfCreator(group);
        Request request = new Request(group, user);
        request = requestRepository.save(request);
        return request;
    }

    public void kickUser(UUID groupId, String username)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException,
            KickedCreatorException, UserKickedIsNotMember {
        if (groupId == null || username == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException("User not found");
        }
        if (!FilterUtils.checkIfLoggedUser(user)) {
            FilterUtils.checkIfCreator(group);
        }
        if (group.getCreatedBy().equals(user)) {
            throw new KickedCreatorException();
        }
        if (!group.getUsers().contains(user)) {
            throw new UserKickedIsNotMember(null);
        }
        group.getUsers().remove(user);
        groupRepository.save(group);
    }

    public void respondRequest(UUID requestId, boolean accept)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException {
        if (requestId == null) {
            throw new EmptyFormFieldsException();
        }
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new InstanceNotFoundException("Request not found"));
        if (!FilterUtils.checkIfLoggedUser(request.getUser())) {
            throw new NotAllowedResourceException("You cannot respond other user's requests");
        }
        if (accept) {
            request.getGroup().getUsers().add(request.getUser());
            request.getUser().getGroups().add(request.getGroup());
            groupRepository.save(request.getGroup());
            userRepository.save(request.getUser());
        }
        requestRepository.delete(request);
    }

}
