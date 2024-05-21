package com.viladev.fundshare.controller;

import java.util.UUID;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.dto.PaymentDto;
import com.viladev.fundshare.service.PaymentService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.ErrorCodes;

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
            return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCodes.NOT_ABOVE_0_AMOUNT, e.getMessage()));
        } catch (PayeeIsNotInGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(ErrorCodes.PAYEE_NOT_IN_GROUP, e.getMessage()));
        } catch (PayerIsNotInGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(ErrorCodes.PAYER_NOT_IN_GROUP, e.getMessage()));
        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(ErrorCodes.CLOSED_GROUP, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<PaymentDto>(new PaymentDto(newPayment)));
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPayment(@PathVariable("id") UUID paymentId)
            throws InstanceNotFoundException, EmptyFormFieldsException {
        Payment payment = paymentService.getPaymentById(paymentId);

        return ResponseEntity.ok().body(new ApiResponse<PaymentDto>(new PaymentDto(payment)));
    }

    @DeleteMapping("/payment/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable("id") UUID paymentId)
            throws InstanceNotFoundException, EmptyFormFieldsException, NotAllowedResourceException {
        try {
            paymentService.deletePayment(paymentId);

        } catch (InactiveGroupException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(ErrorCodes.CLOSED_GROUP, e.getMessage()));
        }
        return ResponseEntity.ok().body(new ApiResponse<Void>());
    }

}