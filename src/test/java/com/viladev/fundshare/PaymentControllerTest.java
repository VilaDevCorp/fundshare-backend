package com.viladev.fundshare;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladev.fundshare.forms.GroupForm;
import com.viladev.fundshare.forms.PaymentForm;
import com.viladev.fundshare.forms.UserPaymentForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Payment;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.dto.PaymentDto;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.PaymentRepository;
import com.viladev.fundshare.repository.UserPaymentRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.service.PaymentService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    private static final String USER_1_EMAIL = "test@gmail.com";
    private static final String USER_1_USERNAME = "test";
    private static final String USER_1_PASSWORD = "12test34";

    private static final String USER_2_EMAIL = "test2@gmail.com";
    private static final String USER_2_USERNAME = "test2";
    private static final String USER_2_PASSWORD = "test1234";

    private static final String USER_3_EMAIL = "test3@gmail.com";
    private static final String USER_3_USERNAME = "test3";
    private static final String USER_3_PASSWORD = "1234test";

    private static final String GROUP_1_NAME = "Group 1";
    private static final String GROUP_1_DESCRIPTION = "This is a group 1 description for testing";
    private static final String GROUP_2_NAME = "Group 2";
    private static final String GROUP_2_DESCRIPTION = "This is a group 2 description for testing";

    private static final String UPDATED_GROUP_NAME = "Updated name";
    private static final String UPDATED_GROUP_DESCRIPTION = "Updated description";

    private static final String NONEXISTING_USER = "unknowuser";
    private static final UUID NONEXISTING_ID = UUID.randomUUID();

    private static UUID GROUP_1_ID;
    private static UUID GROUP_2_ID;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserPaymentRepository userPaymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void initialize() throws Exception {
        User user1 = new User(USER_1_EMAIL, USER_1_USERNAME, USER_1_PASSWORD);
        user1.setValidated(true);
        User user2 = new User(USER_2_EMAIL, USER_2_USERNAME, USER_2_PASSWORD);
        user2.setValidated(true);
        User user3 = new User(USER_3_EMAIL, USER_3_USERNAME, USER_3_PASSWORD);
        user3.setValidated(true);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        Group group1 = new Group(GROUP_1_NAME, GROUP_1_DESCRIPTION, user1);
        group1.setUsers(Set.of(user1, user2, user3));
        groupRepository.save(group1);
        GROUP_1_ID = group1.getId();
        Group group2 = new Group(GROUP_2_NAME, GROUP_2_DESCRIPTION, user1);
        group2.setUsers(Set.of(user1));
        groupRepository.save(group2);
        GROUP_2_ID = group2.getId();
    }

    @AfterEach
    void clean() {
        paymentRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Payment creation")
    class PaymentCreation {
        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_CreatePaymentSuccesful_Ok() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_2_USERNAME, 100.0));
            payees.add(new UserPaymentForm(USER_3_USERNAME, 50.0));

            Set<UserPaymentForm> payees2 = new HashSet<>();
            payees2.add(new UserPaymentForm(USER_2_USERNAME, 100.0));

            PaymentForm form1 = new PaymentForm(GROUP_1_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            String resultString = mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isOk()).andReturn()
                    .getResponse().getContentAsString();

            ApiResponse<PaymentDto> result = null;
            TypeReference<ApiResponse<PaymentDto>> typeReference = new TypeReference<ApiResponse<PaymentDto>>() {
            };

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }

            UUID paymentId = result.getData().getId();
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            assertNotNull(payment);
            assertEquals(payment.getGroup().getId(), GROUP_1_ID);
            assertEquals(payment.getCreatedBy().getUsername(), USER_1_USERNAME);
            assertEquals(payment.getUserPayments().size(), 2);
            assertTrue(
                    payment.getUserPayments().stream()
                            .anyMatch(
                                    up -> up.getUser().getUsername().equals(USER_2_USERNAME) && up.getAmount() == 100.0)
                            && payment
                                    .getUserPayments().stream()
                                    .anyMatch(up -> up.getUser().getUsername().equals(USER_3_USERNAME)
                                            && up.getAmount() == 50.0));

            User user1 = userRepository.findByUsername(USER_1_USERNAME);
            User user2 = userRepository.findByUsername(USER_2_USERNAME);
            User user3 = userRepository.findByUsername(USER_3_USERNAME);

            assertEquals(user1.getBalance(), -150.0);
            assertEquals(user2.getBalance(), 100.0);
            assertEquals(user3.getBalance(), 50.0);
        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_CreatePaymentMandatoryFieldsEmpty_BadRequest() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();

            PaymentForm form1 = new PaymentForm(GROUP_1_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest());
        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_CreatePaymentNonExistingGroup_NotFound() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_2_USERNAME, 100.0));
            payees.add(new UserPaymentForm(USER_3_USERNAME, 50.0));

            PaymentForm form1 = new PaymentForm(NONEXISTING_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isNotFound());
        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_CreatePaymentNonExistingPayee_NotFound() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(NONEXISTING_USER, 100.0));
            payees.add(new UserPaymentForm(USER_3_USERNAME, 50.0));

            PaymentForm form1 = new PaymentForm(GROUP_1_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isNotFound());
        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_CreatePaymentInvalidAmount_BadRequest() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_2_USERNAME, 0.0));
            payees.add(new UserPaymentForm(USER_3_USERNAME, 50.0));

            PaymentForm form1 = new PaymentForm(GROUP_1_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            String resultString = mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest()).andReturn()
                    .getResponse()
                    .getContentAsString();

            ApiResponse<Void> result = null;
            TypeReference<ApiResponse<Void>> typeReference = new TypeReference<ApiResponse<Void>>() {
            };
            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            assertEquals(CodeErrors.NOT_ABOVE_0_AMOUNT, result.getErrorCode());
        }

        @WithMockUser(username = USER_2_USERNAME)
        @Test
        void When_CreatePaymentForGroupAndPayerNotMember_Forbidden() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_1_USERNAME, 10.0));

            PaymentForm form1 = new PaymentForm(GROUP_2_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            String resultString = mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isForbidden()).andReturn()
                    .getResponse()
                    .getContentAsString();

            ApiResponse<Void> result = null;
            TypeReference<ApiResponse<Void>> typeReference = new TypeReference<ApiResponse<Void>>() {
            };
            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            assertEquals(CodeErrors.PAYER_NOT_IN_GROUP, result.getErrorCode());
        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_CreatePaymentForGroupAndPayeeNotMember_Forbidden() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_2_USERNAME, 10.0));

            PaymentForm form1 = new PaymentForm(GROUP_2_ID, payees);

            ObjectMapper obj = new ObjectMapper();

            String resultString = mockMvc.perform(post("/api/payment")
                    .contentType("application/json")
                    .content(obj.writeValueAsString(form1))).andExpect(status().isForbidden()).andReturn()
                    .getResponse()
                    .getContentAsString();

            ApiResponse<Void> result = null;
            TypeReference<ApiResponse<Void>> typeReference = new TypeReference<ApiResponse<Void>>() {
            };
            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }
            assertEquals(CodeErrors.PAYEE_NOT_IN_GROUP, result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Payment search")
    class PaymentSearch {

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_GetPaymentSuccesful_Ok() throws Exception {

            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_2_USERNAME, 100.0));
            payees.add(new UserPaymentForm(USER_3_USERNAME, 50.0));

            PaymentForm form = new PaymentForm(GROUP_1_ID, payees);

            Payment payment = paymentService.createPayment(form);

            ObjectMapper obj = new ObjectMapper();

            ApiResponse<PaymentDto> result = null;
            TypeReference<ApiResponse<PaymentDto>> typeReference = new TypeReference<ApiResponse<PaymentDto>>() {
            };

            String resultString = mockMvc.perform(get("/api/payment/" + payment.getId()))
                    .andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString();

            try {
                result = obj.readValue(resultString, typeReference);
            } catch (Exception e) {
                assertTrue(false, "Error parsing response");
            }

            PaymentDto foundPayment = result.getData();

            assertEquals(foundPayment.getGroup().getId(), GROUP_1_ID);
            assertEquals(foundPayment.getCreatedBy().getUsername(), USER_1_USERNAME);
            assertEquals(foundPayment.getUserPayments().size(), 2);
            assertEquals(foundPayment.getTotalAmount(), 150.0);
            assertTrue(
                    foundPayment.getUserPayments().stream()
                            .anyMatch(
                                    up -> up.getUser().getUsername().equals(USER_3_USERNAME) && up.getAmount() == 50.0)
                            && foundPayment
                                    .getUserPayments().stream()
                                    .anyMatch(up -> up.getUser().getUsername().equals(USER_2_USERNAME)
                                            && up.getAmount() == 100.0));

        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_GetNonExistingPayment_NotFound() throws Exception {

            mockMvc.perform(get("/api/payment/" + NONEXISTING_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Payment deletion")
    class PaymentDeletion {

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_DeletePaymentSuccessful_Ok() throws Exception {
            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_2_USERNAME, 50.0));

            PaymentForm form = new PaymentForm(GROUP_1_ID, payees);

            Payment payment = paymentService.createPayment(form);

            mockMvc.perform(delete("/api/payment/" + payment.getId()))
                    .andExpect(status().isOk());

            assertTrue(paymentRepository.findById(payment.getId()).isEmpty());

            User user1 = userRepository.findByUsername(USER_1_USERNAME);
            User user2 = userRepository.findByUsername(USER_2_USERNAME);

            assertEquals(user1.getBalance(), 0.0);
            assertEquals(user2.getBalance(), 0.0);
        }

        @WithMockUser(username = USER_1_USERNAME)
        @Test
        void When_DeleteNonExistingPayment_NotFound() throws Exception {
            mockMvc.perform(delete("/api/payment/" + NONEXISTING_ID))
                    .andExpect(status().isNotFound());
        }

        @WithMockUser(username = USER_2_USERNAME)
        @Test
        void When_DeleteOtherUserPayment_Forbidden() throws Exception {
            Set<UserPaymentForm> payees = new HashSet<>();
            payees.add(new UserPaymentForm(USER_3_USERNAME, 50.0));

            PaymentForm form = new PaymentForm(GROUP_1_ID, payees);

            Payment payment = paymentService.createPayment(form);
            User otherUser = userRepository.findByUsername(USER_1_USERNAME);
            payment.setCreatedBy(otherUser);
            paymentRepository.save(payment);

            mockMvc.perform(delete("/api/payment/" + payment.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
