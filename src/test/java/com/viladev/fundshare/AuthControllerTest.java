package com.viladev.fundshare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladev.fundshare.forms.LoginForm;
import com.viladev.fundshare.forms.RegisterForm;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.model.ValidationCode;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.repository.ValidationCodeRepository;
import com.viladev.fundshare.service.UserService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;
import com.viladev.fundshare.utils.ValidationCodeTypeEnum;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	private static final String ACTIVE_USER_EMAIL = "test@gmail.com";
	private static final String ACTIVE_USER_USERNAME = "test";
	private static final String ACTIVE_USER_PASSWORD = "12test34";

	private static final String INACTIVE_USER_EMAIL = "test2@gmail.com";
	private static final String INACTIVE_USER_USERNAME = "test2";
	private static final String INACTIVE_USER_PASSWORD = "test1234";

	private static final String NEW_USER_EMAIL = "test3@gmail.com";
	private static final String NEW_USER_USERNAME = "test3";
	private static final String NEW_USER_PASSWORD = "1234test";

	private static final String OTHER_USER_EMAIL = "test4@gmail.com";
	private static final String OTHER_USER_USERNAME = "test4";
	private static final String OTHER_USER_PASSWORD = "1234test1234";

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ValidationCodeRepository validationCodeRepository;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void initialize() throws Exception {

		User user1 = userService.registerUser(ACTIVE_USER_EMAIL, ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD);
		userService.registerUser(INACTIVE_USER_EMAIL, INACTIVE_USER_USERNAME, INACTIVE_USER_PASSWORD);

		user1.setValidated(true);
		userRepository.save(user1);

	}

	@AfterEach
	void clean() {
		userRepository.deleteAll();
	}

	@Test
	void When_ApiHealth_Ok() throws Exception {
		mockMvc.perform(get("/api/public/health"))
				.andExpect(status().isOk());
	}

	@Nested
	@DisplayName("User register")
	class UserRegister {
		@Test
		void When_RegisterUser_Ok() throws Exception {
			RegisterForm form = new RegisterForm(NEW_USER_EMAIL, NEW_USER_USERNAME, NEW_USER_PASSWORD);
			ObjectMapper obj = new ObjectMapper();

			String resultString = mockMvc.perform(post("/api/public/register")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();
			ApiResponse<User> result = null;
			TypeReference<ApiResponse<User>> typeReference = new TypeReference<ApiResponse<User>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}

			// We remove the quotes from the UUID (extra quotes being added)
			String username = result.getData().getUsername();
			User user = userRepository.findByUsername(username);
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			assertEquals(NEW_USER_EMAIL, user.getEmail());
			assertEquals(NEW_USER_USERNAME, user.getUsername());
			assertTrue(passwordEncoder.matches(NEW_USER_PASSWORD, user.getPassword()));
		}

		@Test
		void When_RegisterAlreadyRegisterMail_Conflict() throws Exception {
			RegisterForm form = new RegisterForm(ACTIVE_USER_EMAIL, OTHER_USER_USERNAME, OTHER_USER_PASSWORD);
			ObjectMapper obj = new ObjectMapper();

			String resultString = mockMvc.perform(post("/api/public/register")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isConflict()).andReturn()
					.getResponse().getContentAsString();

			ApiResponse<User> result = null;
			TypeReference<ApiResponse<User>> typeReference = new TypeReference<ApiResponse<User>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}

			// We remove the quotes from the UUID (extra quotes being added)
			String errorCode = result.getErrorCode();
			assertEquals(CodeErrors.EMAIL_ALREADY_IN_USE, errorCode);

		}

		@Test
		void When_RegisterAlreadyRegisterUsername_Conflict() throws Exception {
			RegisterForm form = new RegisterForm(OTHER_USER_EMAIL, ACTIVE_USER_USERNAME, OTHER_USER_PASSWORD);
			ObjectMapper obj = new ObjectMapper();

			String resultString = mockMvc.perform(post("/api/public/register")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isConflict()).andReturn()
					.getResponse().getContentAsString();

			ApiResponse<User> result = null;
			TypeReference<ApiResponse<User>> typeReference = new TypeReference<ApiResponse<User>>() {
			};

			try {
				result = obj.readValue(resultString, typeReference);
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}

			// We remove the quotes from the UUID (extra quotes being added)
			String errorCode = result.getErrorCode();
			assertEquals(CodeErrors.USERNAME_ALREADY_IN_USE, errorCode);

		}

		@Test
		void When_RegisterEmptyMandatoryFields_BadRequest() throws Exception {
			RegisterForm form1 = new RegisterForm(null, OTHER_USER_USERNAME, OTHER_USER_PASSWORD);
			RegisterForm form2 = new RegisterForm(OTHER_USER_EMAIL, null, OTHER_USER_PASSWORD);
			RegisterForm form3 = new RegisterForm(OTHER_USER_EMAIL, OTHER_USER_USERNAME, null);
			ObjectMapper obj = new ObjectMapper();

			mockMvc.perform(post("/api/public/register")
					.contentType("application/json")
					.content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest());
			mockMvc.perform(post("/api/public/register")
					.contentType("application/json")
					.content(obj.writeValueAsString(form2))).andExpect(status().isBadRequest());

			mockMvc.perform(post("/api/public/register")
					.contentType("application/json")
					.content(obj.writeValueAsString(form3))).andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("User login")
	class UserLogin {

		@Test
		void When_LoginEmptyFields_BadRequest() throws Exception {
			LoginForm form1 = new LoginForm(null, ACTIVE_USER_PASSWORD);
			LoginForm form2 = new LoginForm(ACTIVE_USER_USERNAME, null);
			ObjectMapper obj = new ObjectMapper();

			mockMvc.perform(post("/api/public/login")
					.contentType("application/json")
					.content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest());
			mockMvc.perform(post("/api/public/login")
					.contentType("application/json")
					.content(obj.writeValueAsString(form2))).andExpect(status().isBadRequest());
		}

		@Test
		void When_LoginInvalidCredentials_Unauthorized() throws Exception {
			LoginForm form = new LoginForm(ACTIVE_USER_USERNAME, OTHER_USER_PASSWORD);
			ObjectMapper obj = new ObjectMapper();

			mockMvc.perform(post("/api/public/login")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isUnauthorized());

		}

		@Test
		void When_LoginNotActivatedAccount_Forbidden() throws Exception {
			LoginForm form = new LoginForm(INACTIVE_USER_USERNAME, INACTIVE_USER_PASSWORD);
			ObjectMapper obj = new ObjectMapper();

			mockMvc.perform(post("/api/public/login")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isForbidden());
		}

		@Test
		void When_LoginSuccesful_Ok() throws Exception {
			LoginForm form = new LoginForm(ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD);
			ObjectMapper obj = new ObjectMapper();

			String resultString = mockMvc.perform(post("/api/public/login")
					.contentType("application/json")
					.content(obj.writeValueAsString(form))).andExpect(status().isOk())
					.andExpect(cookie().exists("JWT_TOKEN")).andReturn().getResponse().getContentAsString();

			TypeReference<ApiResponse<String>> typeReference = new TypeReference<ApiResponse<String>>() {
			};
			ApiResponse<String> result = null;
			try {
				result = obj.readValue(resultString, typeReference);
				UUID.fromString(result.getData());
			} catch (Exception e) {
				assertTrue(false, "Error parsing response");
			}
		}
	}

	@Nested
	@DisplayName("Account validation")
	class AccountValidation {

		@Test
		void When_AccountValidationWrongCode_Unauthorized() throws Exception {
			ValidationCode validationCode = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
					.get(0);
			mockMvc.perform(
					post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" + validationCode.getCode() + "1"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		void When_AccountValidationExpiredCode_Gone() throws Exception {
			List<ValidationCode> validationCodeList = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType());
			// We take the first validation code (more recent) and we delete the rest to
			// avoid conflicts with other tests
			ValidationCode validationCode = validationCodeList.get(0);
			List<ValidationCode> validationCodeToDelete = validationCodeList.subList(1, validationCodeList.size());
			validationCodeToDelete.forEach(vc -> validationCodeRepository.delete(vc));
			Calendar oldCreationDate = (Calendar) validationCode.getCreatedAt().clone();
			Calendar expiredDate = (Calendar) oldCreationDate.clone();
			expiredDate.add(Calendar.MINUTE, -ValidationCode.EXPIRATION_MINUTES - 1);
			validationCode.setCreatedAt(expiredDate);
			validationCodeRepository.save(validationCode);
			mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" + validationCode.getCode()))
					.andExpect(status().isGone());
			validationCode.setCreatedAt(oldCreationDate);
			validationCodeRepository.save(validationCode);

		}

		@Test
		void When_AccountValidationAlreadyUsedCode_Conflict() throws Exception {
			ValidationCode validationCode = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
					.get(0);
			validationCode.setUsed(true);
			validationCodeRepository.save(validationCode);
			mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" + validationCode.getCode()))
					.andExpect(status().isConflict());
			validationCode.setUsed(false);
			validationCodeRepository.save(validationCode);
		}

		@Test
		void When_AccountValidationSuccesful_Ok() throws Exception {
			ValidationCode validationCode = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
					.get(0);
			mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" + validationCode.getCode()))
					.andExpect(status().isOk());
			User user = userRepository.findByUsername(INACTIVE_USER_USERNAME);
			assertTrue(user.isValidated());
			validationCode = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
					.get(0);
			assertTrue(validationCode.isUsed());
			user.setValidated(false);
			userRepository.save(user);
			validationCode.setUsed(false);
			validationCodeRepository.save(validationCode);
		}

		@Test
		void When_AccountValidationResendCode_Ok() throws Exception {
			ValidationCode oldValidationCode = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
					.get(0);
			mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/resend"))
					.andExpect(status().isOk());
			ValidationCode newValidationCode = validationCodeRepository
					.findByUserUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
							ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
					.get(0);
			assertNotEquals(oldValidationCode.getCode(), newValidationCode.getCode());
		}
	}

	@Test
	void When_ResetPassword_Ok() throws Exception {
		mockMvc.perform(post("/api/public/forgottenpassword/" + ACTIVE_USER_USERNAME))
				.andExpect(status().isOk());

		ValidationCode validationCode = validationCodeRepository.findByUserUsernameAndTypeOrderByCreatedAtDesc(
				ACTIVE_USER_USERNAME, ValidationCodeTypeEnum.RESET_PASSWORD.getType()).get(0);

		mockMvc.perform(post("/api/public/resetpassword/" + ACTIVE_USER_USERNAME + "/" + validationCode.getCode())
				.content(OTHER_USER_PASSWORD)).andExpect(status().isOk());

		LoginForm formOldPass = new LoginForm(ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD);
		LoginForm formNewPass = new LoginForm(ACTIVE_USER_USERNAME, OTHER_USER_PASSWORD);
		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(post("/api/public/login")
				.contentType("application/json")
				.content(obj.writeValueAsString(formOldPass))).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/public/login")
				.contentType("application/json")
				.content(obj.writeValueAsString(formNewPass))).andExpect(status().isOk());

		User user = userRepository.findByUsername(ACTIVE_USER_USERNAME);
		user.setPassword(new BCryptPasswordEncoder().encode(ACTIVE_USER_PASSWORD));
		userRepository.save(user);

	}

}
