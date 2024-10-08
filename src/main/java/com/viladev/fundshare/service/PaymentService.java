package com.viladev.fundshare.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viladev.fundshare.exceptions.NotAbove0AmountException;
import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.InactiveGroupException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.PayeeIsNotInGroupException;
import com.viladev.fundshare.exceptions.PayerIsNotInGroupException;
import com.viladev.fundshare.forms.PaymentForm;
import com.viladev.fundshare.forms.SearchPaymentForm;
import com.viladev.fundshare.forms.UserPaymentForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.UserPayment;
import com.viladev.fundshare.model.dto.DebtDto;
import com.viladev.fundshare.model.dto.GroupDebtDto;
import com.viladev.fundshare.model.dto.PageDto;
import com.viladev.fundshare.model.dto.PaymentDto;
import com.viladev.fundshare.model.dto.UserDto;
import com.viladev.fundshare.model.dto.UserPaymentDto;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.GroupUserRepository;
import com.viladev.fundshare.repository.PaymentRepository;
import com.viladev.fundshare.repository.UserPaymentRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.utils.AuthUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final PaymentRepository paymentRepository;

    private final UserPaymentRepository userPaymentRepository;

    private final GroupUserRepository groupUserRepository;

    private final MinioService minioService;

    @Autowired
    public PaymentService(GroupRepository groupRepository, UserRepository userRepository,
            PaymentRepository paymentRepository,
            UserPaymentRepository userPaymentRepository, GroupUserRepository groupUserRepository,
            MinioService minioService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.userPaymentRepository = userPaymentRepository;
        this.groupUserRepository = groupUserRepository;
        this.minioService = minioService;
    }

    public Payment createPayment(PaymentForm paymentForm)
            throws EmptyFormFieldsException, NotAbove0AmountException, InstanceNotFoundException,
            PayerIsNotInGroupException,
            PayeeIsNotInGroupException, InactiveGroupException {
        if (paymentForm.getDescription() == null || paymentForm.getDescription().isEmpty()
                || paymentForm.getPayees() == null || paymentForm.getPayees().isEmpty()
                || paymentForm.getGroupId() == null) {
            throw new EmptyFormFieldsException();
        }

        User creator = userRepository.findByUsername(AuthUtils.getUsername());

        Group group = groupRepository.findById(paymentForm.getGroupId())
                .orElseThrow(() -> new InstanceNotFoundException("Group not found"));

        if (!group.isActive()) {
            throw new InactiveGroupException();
        }

        if (!groupUserRepository.existsByGroupIdAndUserUsername(group.getId(), creator.getUsername())) {
            throw new PayerIsNotInGroupException();
        }

        Payment payment = new Payment(paymentForm.getDescription(), creator, group);
        payment = paymentRepository.save(payment);

        Double totalAmount = 0.0;
        for (UserPaymentForm userPaymentForm : paymentForm.getPayees()) {
            User user = userRepository.findByUsernameAndValidatedTrue(userPaymentForm.getUsername());
            if (user == null) {
                throw new InstanceNotFoundException("User not found");
            }
            if (!groupUserRepository.existsByGroupIdAndUserUsername(group.getId(), user.getUsername())) {
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
            NotAllowedResourceException, InactiveGroupException {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new InstanceNotFoundException());
        AuthUtils.checkIfCreator(payment);
        Double totalAmount = 0.0;
        Group group = payment.getGroup();
        if (!group.isActive()) {
            throw new InactiveGroupException();
        }

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

    @Transactional(readOnly = true)
    public Double calculateUserGroupBalance(String username, UUID groupId) throws InstanceNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException();
        }
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new InstanceNotFoundException());
        Set<UserPayment> groupPayments = userPaymentRepository.findOperationsInGroupRelatedToUser(groupId,
                user.getUsername());
        Double totalBalance = 0.0;
        for (UserPayment userPayment : groupPayments) {
            // If the payment was sent to the user, add the amount to the total balance
            if (userPayment.getUser().getUsername().equals(username)) {
                totalBalance += userPayment.getAmount();
            } else {
                // If not, the user would be the payer, so subtract the amount from the total
                // balance
                totalBalance -= userPayment.getAmount();
            }
        }
        return totalBalance;
    }

    @Transactional(readOnly = true)
    public PageDto<PaymentDto> searchPayments(SearchPaymentForm searchForm) {
        Pageable pageable = PageRequest.of(searchForm.getPage(), searchForm.getPageSize());
        Slice<PaymentDto> payments;
        if (searchForm.isUserRelated()) {
            String loggedUsername = AuthUtils.getUsername();
            payments = paymentRepository.findByRelatedUsername(loggedUsername,
                    pageable);
        } else {
            UUID groupId = UUID.fromString(searchForm.getGroupId());
            payments = paymentRepository.findByGroupIdAndCreatedByUsername(groupId, null,
                    pageable);
        }
        return new PageDto<>(searchForm.getPage(), payments.hasNext(), payments.getContent());
    }

    @Transactional(readOnly = true)
    public PageDto<DebtDto> searchDebts(String groupId, boolean ownDebt) throws NotAllowedResourceException {
        UUID uuidGroupId = groupId == null ? null : UUID.fromString(groupId);
        String loggedUsername = AuthUtils.getUsername();
        if (uuidGroupId != null && !groupUserRepository.existsByGroupIdAndUserUsername(uuidGroupId, loggedUsername)) {
            throw new NotAllowedResourceException("User is not in the group");
        }
        String username = ownDebt ? loggedUsername : null;

        List<UserPaymentDto> userPayments = userPaymentRepository
                .findByGroupIdAndRelatedUser(uuidGroupId, username, null);

        List<DebtDto> debts = new ArrayList<>();

        for (UserPaymentDto userPayment : userPayments) {
            // If the user has paid to himself, we skip the payment
            if (userPayment.getPayment().getCreatedBy().getUsername().equals(userPayment.getUser().getUsername())) {
                continue;
            }
            DebtDto debtElement = debts.stream()
                    .filter((debt) -> (debt.getPayer().getUsername().equals(userPayment.getUser().getUsername())
                            && debt.getPayee().getUsername()
                                    .equals(userPayment.getPayment().getCreatedBy().getUsername()))
                            || (debt.getPayer().getUsername()
                                    .equals(userPayment.getPayment().getCreatedBy().getUsername())
                                    && debt.getPayee().getUsername().equals(userPayment.getUser().getUsername())))
                    .findFirst().orElse(null);
            if (debtElement == null) {
                try {
                    userPayment.getUser()
                            .setPictureUrl(minioService.getProfilePictureUrl(userPayment.getUser().getUsername()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    userPayment.getPayment().getCreatedBy()
                            .setPictureUrl(minioService
                                    .getProfilePictureUrl(userPayment.getPayment().getCreatedBy().getUsername()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                debtElement = new DebtDto(userPayment.getPayment().getCreatedBy(), userPayment.getUser(),
                        userPayment.getAmount());
                debts.add(debtElement);
            } else {

                if (debtElement.getPayer().getUsername()
                        .equals(userPayment.getPayment().getCreatedBy().getUsername())) {
                    debtElement.setAmount(debtElement.getAmount() + userPayment.getAmount());
                } else {
                    debtElement.setAmount(debtElement.getAmount() - userPayment.getAmount());
                }
            }
        }
        List<DebtDto> cleanList = new ArrayList<>();
        for (DebtDto debt : debts) {
            if (debt.getAmount() == 0) {
                continue;
            }
            if (debt.getAmount() > 0) {
                cleanList.add(debt);
            } else {
                cleanList.add(new DebtDto(debt.getPayee(), debt.getPayer(), -debt.getAmount()));
            }
        }
        List<DebtDto> orderedList = cleanList.stream()
                .sorted((debt1, debt2) -> debt2.getAmount().compareTo(debt1.getAmount())).toList();

        return new PageDto<>(0, false, orderedList);
    }

    @Transactional(readOnly = true)
    public List<GroupDebtDto> getDebtsFromUserByGroup(String username)
            throws NotAllowedResourceException, InstanceNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InstanceNotFoundException();
        }
        String loggedUsername = AuthUtils.getUsername();
        List<UserPaymentDto> userPayments = userPaymentRepository.findByGroupIdAndRelatedUser(null, username,
                loggedUsername);

        List<GroupDebtDto> debtList = new ArrayList<>();

        for (UserPaymentDto up : userPayments) {
            Optional<GroupDebtDto> groupDebt = debtList.stream()
                    .filter((debt) -> debt.getGroup().getId().equals(up.getPayment().getGroup().getId()))
                    .findFirst();
            if (groupDebt.isEmpty()) {
                if (up.getPayment().getCreatedBy().getUsername().equals(loggedUsername)) {
                    GroupDebtDto newGroupDebt = new GroupDebtDto(up.getPayment().getGroup(), up.getAmount());
                    debtList.add(newGroupDebt);
                } else {
                    GroupDebtDto newGroupDebt = new GroupDebtDto(up.getPayment().getGroup(), -up.getAmount());
                    debtList.add(newGroupDebt);
                }
            } else {
                if (up.getPayment().getCreatedBy().getUsername().equals(loggedUsername)) {
                    groupDebt.get().setAmount(groupDebt.get().getAmount() + up.getAmount());
                } else {
                    groupDebt.get().setAmount(groupDebt.get().getAmount() - up.getAmount());
                }
            }
        }

        debtList.removeIf((debt) -> debt.getAmount() == 0);
        List<GroupDebtDto> orderedList = debtList.stream()
                .sorted((debt1, debt2) -> ((Double) Math.abs(debt2.getAmount())).compareTo(Math.abs(debt1.getAmount())))
                .toList();

        return orderedList;

    }

}
