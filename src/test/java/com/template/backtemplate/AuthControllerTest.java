package com.template.backtemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.backtemplate.forms.LoginForm;
import com.template.backtemplate.forms.RegisterForm;
import com.template.backtemplate.model.User;
import com.template.backtemplate.model.ValidationCode;
import com.template.backtemplate.repository.UserRepository;
import com.template.backtemplate.repository.ValidationCodeRepository;
import com.template.backtemplate.utils.ApiResponse;
import com.template.backtemplate.utils.CodeErrors;
import com.template.backtemplate.utils.ValidationCodeTypeEnum;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
	private ValidationCodeRepository validationCodeRepository;

	@Autowired
	private MockMvc mockMvc;

	@AfterAll
	void tearDown() {
		validationCodeRepository.deleteAll();
		userRepository.deleteAll();
	}

	@BeforeAll
	void initialize() throws Exception {
		userRepository.deleteAll();
		RegisterForm form1 = new RegisterForm(ACTIVE_USER_EMAIL, ACTIVE_USER_USERNAME, ACTIVE_USER_PASSWORD);
		RegisterForm form2 = new RegisterForm(INACTIVE_USER_EMAIL, INACTIVE_USER_USERNAME, INACTIVE_USER_PASSWORD);

		ObjectMapper obj = new ObjectMapper();
		mockMvc.perform(post("/api/public/register")
				.contentType("application/json")
				.content(obj.writeValueAsString(form1)));
		mockMvc.perform(post("/api/public/register")
				.contentType("application/json")
				.content(obj.writeValueAsString(form2)));

		ValidationCode validationCode = validationCodeRepository
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(ACTIVE_USER_USERNAME,
						ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
				.get(0);
		mockMvc.perform(post("/api/public/validate/" + ACTIVE_USER_USERNAME + "/" + validationCode.getCode()));

	}

	@Test
	void When_ApiHealth_Ok() throws Exception {
		mockMvc.perform(get("/api/public/health"))
				.andExpect(status().isOk());
	}

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
		UUID id = result.getData().getId();
		User user = userRepository.findById(id).get();
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

	@Test
	void When_AccountValidationWrongCode_Unauthorized() throws Exception {
		ValidationCode validationCode = validationCodeRepository
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
						ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
				.get(0);
		mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" + validationCode.getCode() + "1"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void When_AccountValidationExpiredCode_Gone() throws Exception {
		List<ValidationCode> validationCodeList = validationCodeRepository
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
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
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
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
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
						ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
				.get(0);
		mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" + validationCode.getCode()))
				.andExpect(status().isOk());
		User user = userRepository.findByUsername(INACTIVE_USER_USERNAME);
		assertTrue(user.isValidated());
		validationCode = validationCodeRepository
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
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
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
						ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
				.get(0);
		mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/resend"))
				.andExpect(status().isOk());
		ValidationCode newValidationCode = validationCodeRepository
				.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
						ValidationCodeTypeEnum.ACTIVATE_ACCOUNT.getType())
				.get(0);
		assertNotEquals(oldValidationCode.getCode(), newValidationCode.getCode());
	}

	@Test
	void When_ResetPassword_Ok() throws Exception {
		mockMvc.perform(post("/api/public/forgottenpassword/" + ACTIVE_USER_USERNAME))
				.andExpect(status().isOk());

		ValidationCode validationCode = validationCodeRepository.findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(
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

	// @Test
	// void When_AccountValidation_Conflict() throws Exception {
	// ValidationCode validationCode = validationCodeRepository
	// .findByCreatedByUsernameAndTypeOrderByCreatedAtDesc(INACTIVE_USER_USERNAME,
	// false,
	// ValidationCodeTypeEnum.ACTIVATE_ACCOUNT)
	// .get(0);
	// validationCode.setUsed(true);
	// validationCodeRepository.save(validationCode);
	// mockMvc.perform(post("/api/public/validate/" + INACTIVE_USER_USERNAME + "/" +
	// validationCode.getCode()))
	// .andExpect(status().isConflict());
	// validationCode.setUsed(false);
	// validationCodeRepository.save(validationCode);

	// }
}
