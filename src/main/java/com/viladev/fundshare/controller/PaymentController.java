package com.viladev.fundshare.controller;

import java.util.List;
import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladev.fundshare.exceptions.EmptyFormFieldsException;
import com.viladev.fundshare.exceptions.InactiveGroupException;
import com.viladev.fundshare.exceptions.NotAbove0AmountException;
import com.viladev.fundshare.exceptions.NotAllowedResourceException;
import com.viladev.fundshare.exceptions.PayeeIsNotInGroupException;
import com.viladev.fundshare.exceptions.PayerIsNotInGroupException;
import com.viladev.fundshare.forms.PaymentForm;
import com.viladev.fundshare.forms.SearchDebtForm;
import com.viladev.fundshare.forms.SearchPaymentForm;
import com.viladev.fundshare.forms.SearchRequestForm;
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.dto.DebtDto;
import com.viladev.fundshare.model.dto.GroupDebtDto;
import com.viladev.fundshare.model.dto.PageDto;
import com.viladev.fundshare.model.dto.PaymentDto;
import com.viladev.fundshare.model.dto.RequestDto;
import com.viladev.fundshare.service.PaymentService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@RequestBody PaymentForm paymentForm)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAbove0AmountException {
        Payment newPayment = null;
        try {
            newPayment = paymentService.createPayment(paymentForm);
        } catch (NotAbove0AmountException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(CodeErrors.NOT_ABOVE_0_AMOUNT, e.getMessage()));
        } catch (PayeeIsNotInGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.PAYEE_NOT_IN_GROUP, e.getMessage()));
        } catch (PayerIsNotInGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.PAYER_NOT_IN_GROUP, e.getMessage()));
        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.CLOSED_GROUP, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<PaymentDto>(new PaymentDto(newPayment)));
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPayment(@PathVariable("id") UUID paymentId)
            throws InstanceNotFoundException, EmptyFormFieldsException {
        Payment payment = paymentService.getPaymentById(paymentId);

        return ResponseEntity.ok().body(new ApiResponse<PaymentDto>(new PaymentDto(payment)));
    }

    @PostMapping("/payment/search")
    public ResponseEntity<ApiResponse<PageDto<PaymentDto>>> searchPayments(@RequestBody SearchPaymentForm searchForm)
            throws InstanceNotFoundException, NotAllowedResourceException {
        PageDto<PaymentDto> result = paymentService.searchPayments(searchForm);
        return ResponseEntity.ok().body(new ApiResponse<>(result));
    }

    @DeleteMapping("/payment/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable("id") UUID paymentId)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {
        try {
            paymentService.deletePayment(paymentId);

        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(CodeErrors.CLOSED_GROUP, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<Void>());
    }

    @PostMapping("/debt/search")
    public ResponseEntity<ApiResponse<PageDto<DebtDto>>> searchDebts(@RequestBody SearchDebtForm searchForm)
            throws InstanceNotFoundException, NotAllowedResourceException {
        PageDto<DebtDto> result = paymentService.searchDebts(searchForm.getGroupId(), searchForm.isOwnDebts());
        return ResponseEntity.ok().body(new ApiResponse<>(result));
    }

    @GetMapping("/debt/{username}")
    public ResponseEntity<ApiResponse<List<GroupDebtDto>>> getDebtWithUser(@PathVariable String username)
            throws InstanceNotFoundException, NotAllowedResourceException {
        List<GroupDebtDto> result = paymentService.getDebtsFromUserByGroup(username);
        return ResponseEntity.ok().body(new ApiResponse<>(result));
    }
}