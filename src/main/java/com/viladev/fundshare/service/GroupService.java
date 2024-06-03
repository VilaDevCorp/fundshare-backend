package com.viladev.fundshare.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.InactiveGroupException;
import com.viladev.fundshare.exceptions.KickedCreatorException;
import com.viladev.fundshare.exceptions.NonZeroBalanceException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.UserAlreadyInvitedException;
import com.viladev.fundshare.exceptions.UserAlreadyPresentException;
import com.viladev.fundshare.exceptions.UserKickedIsNotMember;
import com.viladev.fundshare.forms.SearchGroupForm;
import com.viladev.fundshare.forms.SearchRequestForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.GroupUser;
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.UserPayment;
import com.viladev.fundshare.model.dto.GroupDto;
import com.viladev.fundshare.model.dto.PageDto;
import com.viladev.fundshare.model.dto.RequestDto;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.GroupUserRepository;
import com.viladev.fundshare.repository.PaymentRepository;
import com.viladev.fundshare.repository.RequestRepository;
import com.viladev.fundshare.repository.UserPaymentRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.utils.AuthUtils;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final RequestRepository requestRepository;

    private final PaymentRepository paymentRepository;

    private final UserPaymentRepository userPaymentRepository;

    private final PaymentService paymentService;

    private final GroupUserRepository groupUserRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
            RequestRepository requestRepository, PaymentRepository paymentRepository,
            UserPaymentRepository userPaymentRepository, PaymentService paymentService,
            GroupUserRepository groupUserRepository) {

        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.paymentRepository = paymentRepository;
        this.userPaymentRepository = userPaymentRepository;
        this.paymentService = paymentService;
        this.groupUserRepository = groupUserRepository;
    }

    public Group createGroup(String name, String description) throws EmptyFormFieldsException {
        User creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (name == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = new Group(name, description, creator);
        group = groupRepository.save(group);
        groupUserRepository.save(new GroupUser(creator, group));
        return group;
    }

    public Group editGroup(UUID id, String name, String description)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException,
            InactiveGroupException {
        if (id == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        if (!group.isActive()) {
            throw new InactiveGroupException();
        }
        AuthUtils.checkIfCreator(group);

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

    @Transactional(readOnly = true)
    public PageDto<GroupDto> searchGroups(SearchGroupForm form) {
        int page = form.getPageSize() == null ? 0 : form.getPage();
        int pageSize = form.getPageSize() == null ? 100000 : form.getPageSize();
        Pageable pageable = PageRequest.of(page, pageSize);
        User user = userRepository.findByUsername(AuthUtils.getUsername());
        String keyword = null;
        if (form.getKeyword() != null) {
            keyword = "%" + form.getKeyword().toLowerCase() + "%";
        }
        Slice<GroupDto> result = groupRepository.advancedSearch(user, keyword, pageable);
        return new PageDto<>(form.getPage(), result.hasNext(), result.getContent());
    }

    private void resetPayments(Set<Payment> payments) {
        Map<UUID, Double> userBalances = new HashMap<>();
        // we loop through all the payments
        payments.stream().forEach(payment -> {
            // We use this variable to store the total amount of the payment to add it to
            // the payer's balance
            AtomicReference<Double> totalAmount = new AtomicReference<>(0.0);

            // we loop through all the userPayments of the payment updating the payee's
            // balances
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
            paymentRepository.delete(payment);
        });

        userBalances.entrySet().stream().forEach(entry -> {
            User user = userRepository.findById(entry.getKey()).get();
            user.setBalance(user.getBalance() + entry.getValue());
            userRepository.save(user);
        });
    }

    private void resetUserPayments(Set<UserPayment> userPayments) {
        Map<UUID, Double> userBalances = new HashMap<>();
        userPayments.stream().forEach(userPayment -> {
            UUID payerId = userPayment.getPayment().getCreatedBy().getId();
            UUID payeeId = userPayment.getUser().getId();
            Double amount = userPayment.getAmount();

            if (userBalances.containsKey(payerId)) {
                userBalances.put(payerId,
                        userBalances.get(payerId) + amount);
            } else {
                userBalances.put(payerId, amount);
            }
            if (userBalances.containsKey(payeeId)) {
                userBalances.put(payeeId,
                        userBalances.get(payeeId) - amount);
            } else {
                userBalances.put(payeeId, -amount);
            }
            userPaymentRepository.delete(userPayment);
        });
        userBalances.entrySet().stream().forEach(entry -> {
            User user = userRepository.findById(entry.getKey()).get();
            user.setBalance(user.getBalance() + entry.getValue());
            userRepository.save(user);
        });
    }

    public void deleteGroup(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Group group = groupRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        AuthUtils.checkIfCreator(group);
        Set<Payment> payments = paymentRepository.findByGroupId(id);
        resetPayments(payments);
        groupRepository.delete(group);
    }

    public Set<Request> createRequests(UUID groupId, String[] usernames)
            throws InstanceNotFoundException, NotAllowedResourceException, UserAlreadyPresentException,
            UserAlreadyInvitedException, EmptyFormFieldsException, InactiveGroupException {
        if (groupId == null || usernames == null || usernames.length == 0) {
            throw new EmptyFormFieldsException();
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));
        if (!group.isActive()) {
            throw new InactiveGroupException();
        }

        Set<Request> requests = new HashSet<>();

        for (String username : usernames) {
            User user = userRepository.findByUsernameAndValidatedTrue(username);
            if (user == null) {
                throw new InstanceNotFoundException("User not found");
            }
            if (groupUserRepository.existsByGroupIdAndUserUsername(groupId, user.getUsername())) {
                continue;
            }
            if (requestRepository.findByGroupIdAndUserId(groupId, user.getId()) != null) {
                continue;
            }
            AuthUtils.checkIfCreator(group);
            Request request = new Request(group, user);
            request = requestRepository.save(request);
            requests.add(request);
        }
        return requests;
    }

    private void removeUserFromGroup(Group group, String username) {
        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserUsername(group.getId(), username);
        group.getGroupUsers().remove(groupUser);
        groupRepository.save(group);
        groupUserRepository.delete(groupUser);
    }

    // The user can only kick himself (payments are kept) The creator of the group
    // can kick any user (deleted all the operations where the user is involved)
    public void kickUser(UUID groupId, String username)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException,
            KickedCreatorException, UserKickedIsNotMember, NonZeroBalanceException, InactiveGroupException {
        if (groupId == null || username == null) {
            throw new EmptyFormFieldsException();
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));
        if (!group.isActive()) {
            throw new InactiveGroupException();
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException("User not found");
        }
        if (group.getCreatedBy().equals(user)) {
            throw new KickedCreatorException();
        }
        if (!groupUserRepository.existsByGroupIdAndUserUsername(groupId, user.getUsername())) {
            throw new UserKickedIsNotMember(null);
        }

        if (AuthUtils.checkIfLoggedUser(user)) {
            Double userBalanceInGroup = paymentService.calculateUserGroupBalance(username, groupId);
            if (userBalanceInGroup != 0) {
                throw new NonZeroBalanceException("You cannot leave the group with a non-zero balance");
            }
            removeUserFromGroup(group, username);
        } else {
            AuthUtils.checkIfCreator(group);
            Set<Payment> payments = paymentRepository.findByGroupIdAndCreatedByUsername(groupId, username);
            resetPayments(payments);
            Set<UserPayment> userPayments = userPaymentRepository.findByPaymentGroupIdAndUserUsername(groupId,
                    username);
            resetUserPayments(userPayments);
            removeUserFromGroup(group, username);
        }

    }

    public PageDto<RequestDto> findRequestsOfUser(SearchRequestForm form)
            throws InstanceNotFoundException, NotAllowedResourceException {
        String username = AuthUtils.getUsername();
        User user = userRepository.findByUsername(username);

        int page = form.getPageSize() == null ? 0 : form.getPage();
        int pageSize = form.getPageSize() == null ? 100000 : form.getPageSize();
        Pageable pageable = PageRequest.of(page, pageSize);

        if (form.getGroupId() != null) {
            UUID groupId = UUID.fromString(form.getGroupId());
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new InstanceNotFoundException("Group not found"));
            if (group.getGroupUsers().stream().noneMatch(groupUser -> groupUser.getUser().equals(user))) {
                throw new NotAllowedResourceException("You are not a member of this group");
            }
            Slice<RequestDto> result = requestRepository.findByGroupId(
                    groupId, pageable);
            return new PageDto<>(form.getPage(), result.hasNext(), result.getContent());
        } else {
            Slice<RequestDto> result = requestRepository.findByUserIdOrderByCreatedAt(user.getId(), pageable);
            return new PageDto<>(form.getPage(), result.hasNext(), result.getContent());
        }
    }

    public void respondRequest(UUID requestId, boolean accept)
            throws InstanceNotFoundException, NotAllowedResourceException, EmptyFormFieldsException,
            InactiveGroupException {
        if (requestId == null) {
            throw new EmptyFormFieldsException();
        }
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new InstanceNotFoundException("Request not found"));
        if (!AuthUtils.checkIfLoggedUser(request.getUser())) {
            throw new NotAllowedResourceException("You cannot respond other user's requests");
        }
        if (accept) {
            if (!request.getGroup().isActive()) {
                throw new InactiveGroupException();
            }
            groupUserRepository.save(new GroupUser(request.getUser(), request.getGroup()));
        }
        requestRepository.delete(request);
    }

    public void closeGroup(UUID groupId) throws InstanceNotFoundException, NotAllowedResourceException {
        Optional<Group> group = groupRepository.findById(groupId);
        if (!group.isPresent()) {
            throw new InstanceNotFoundException("Group not found");
        }
        AuthUtils.checkIfCreator(group.get());
        group.get().setActive(false);
        Set<Request> requests = group.get().getRequests();
        requests.stream().forEach(request -> {
            requestRepository.delete(request);
        });
        groupRepository.save(group.get());

    }

    public void deleteRequest(UUID id) throws InstanceNotFoundException, NotAllowedResourceException {
        Request request = requestRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        AuthUtils.checkIfCreator(request.getGroup());
        requestRepository.delete(request);
    }

}
