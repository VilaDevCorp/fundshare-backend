package com.viladev.fundshare;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladev.fundshare.forms.GroupForm;
import com.viladev.fundshare.forms.RequestForm;
import com.viladev.fundshare.model.Group;
import com.viladev.fundshare.model.Request;
import com.viladev.fundshare.model.User;
import com.viladev.fundshare.repository.GroupRepository;
import com.viladev.fundshare.repository.RequestRepository;
import com.viladev.fundshare.repository.UserRepository;
import com.viladev.fundshare.repository.ValidationCodeRepository;
import com.viladev.fundshare.service.GroupService;
import com.viladev.fundshare.service.UserService;
import com.viladev.fundshare.utils.ApiResponse;
import com.viladev.fundshare.utils.CodeErrors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class GroupControllerTest {

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

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	@WithMockUser(username = USER_1_USERNAME)
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
		groupRepository.save(group1);
		GROUP_1_ID = group1.getId();
	}

	@AfterAll
	void clean() {
		groupRepository.deleteAll();
		userRepository.deleteAll();
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_CreateGroupEmptyMandatory_BadRequest() throws Exception {
		GroupForm form = new GroupForm(null, "");

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(post("/api/group")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isBadRequest());

	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_CreateGroupSuccesful_Ok() throws Exception {
		GroupForm form = new GroupForm(GROUP_2_NAME, GROUP_2_DESCRIPTION);

		ObjectMapper obj = new ObjectMapper();

		String resultString = mockMvc.perform(post("/api/group")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		ApiResponse<Group> result = null;
		TypeReference<ApiResponse<Group>> typeReference = new TypeReference<ApiResponse<Group>>() {
		};

		try {
			result = obj.readValue(resultString, typeReference);
		} catch (Exception e) {
			assertTrue(false, "Error parsing response");
		}

		// We remove the quotes from the UUID (extra quotes being added)
		UUID groupId = result.getData().getId();
		Group group = groupRepository.getReferenceById(groupId);
		assertEquals(GROUP_2_NAME, group.getName());
		assertEquals(GROUP_2_DESCRIPTION, group.getDescription());

	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_FindGroupSuccesful_Ok() throws Exception {
		ObjectMapper obj = new ObjectMapper();

		String resultString = mockMvc.perform(get("/api/group/" + GROUP_1_ID)).andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		ApiResponse<Group> result = null;
		TypeReference<ApiResponse<Group>> typeReference = new TypeReference<ApiResponse<Group>>() {
		};

		try {
			result = obj.readValue(resultString, typeReference);
		} catch (Exception e) {
			assertTrue(false, "Error parsing response");
		}

		Group group = result.getData();
		assertEquals(GROUP_1_NAME, group.getName());
		assertEquals(GROUP_1_DESCRIPTION, group.getDescription());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_FindGroupNotFound_NotFound() throws Exception {

		mockMvc.perform(get("/api/group/" + UUID.randomUUID())).andExpect(status().isNotFound());

	}

	@WithMockUser(username = USER_2_USERNAME)
	@Test
	void When_EditGroupNotAllowed_Forbidden() throws Exception {

		Group group1 = groupService.getGroup(GROUP_1_ID);

		GroupForm form = new GroupForm(group1.getId(), UPDATED_GROUP_NAME, UPDATED_GROUP_DESCRIPTION);

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(patch("/api/group")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isForbidden());

	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_EditGroupEmptyMandatoryField_BadRequest() throws Exception {

		GroupForm form = new GroupForm(null, UPDATED_GROUP_NAME, UPDATED_GROUP_DESCRIPTION);

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(patch("/api/group")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isBadRequest());

	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_EditGroupSuccesful_Ok() throws Exception {

		GroupForm form = new GroupForm(GROUP_1_ID, UPDATED_GROUP_NAME, UPDATED_GROUP_DESCRIPTION);

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(patch("/api/group")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isOk());

		Group group = groupRepository.getReferenceById(GROUP_1_ID);
		assertEquals(UPDATED_GROUP_NAME, group.getName());
		assertEquals(UPDATED_GROUP_DESCRIPTION, group.getDescription());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_InviteToGroupSuccesful_Ok() throws Exception {

		RequestForm form = new RequestForm(GROUP_1_ID, USER_2_USERNAME);

		ObjectMapper obj = new ObjectMapper();

		String resultString = mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
		TypeReference<ApiResponse<Request>> typeReference = new TypeReference<ApiResponse<Request>>() {
		};
		ApiResponse<Request> result = null;

		try {
			result = obj.readValue(resultString, typeReference);
		} catch (Exception e) {
			assertTrue(false, "Error parsing response");
		}
		Request request = result.getData();
		assertEquals(GROUP_1_ID, request.getGroup().getId());
		assertEquals(USER_2_USERNAME, request.getUser().getUsername());
	}

	@WithMockUser(username = USER_2_USERNAME)
	@Test
	void When_InviteToGroupNotOwned_Forbidden() throws Exception {

		RequestForm form = new RequestForm(GROUP_1_ID, USER_3_USERNAME);

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isForbidden());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_InviteToGroupEmptyMandatoryFields_BadRequest() throws Exception {

		RequestForm form1 = new RequestForm(null, USER_3_USERNAME);
		RequestForm form2 = new RequestForm(GROUP_1_ID, null);

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form1))).andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form2))).andExpect(status().isBadRequest());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_InviteToGroupUserAlreadyPresent_Conflict() throws Exception {

		Group group = groupRepository.getReferenceById(GROUP_1_ID);
		List<User> newUsers = new ArrayList<>(group.getUsers());
		newUsers.add(userRepository.findByUsername(USER_2_USERNAME));
		group.setUsers(newUsers);
		groupRepository.save(group);
		RequestForm form = new RequestForm(GROUP_1_ID, USER_2_USERNAME);

		ObjectMapper obj = new ObjectMapper();

		String resultString = mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isConflict()).andReturn().getResponse()
				.getContentAsString();

		ApiResponse<Request> result = null;
		TypeReference<ApiResponse<Request>> typeReference = new TypeReference<ApiResponse<Request>>() {
		};

		try {
			result = obj.readValue(resultString, typeReference);
		} catch (Exception e) {
			assertTrue(false, "Error parsing response");
		}
		String errorCode = result.getErrorCode();
		assertEquals(CodeErrors.ALREADY_MEMBER_GROUP, errorCode);

	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_InviteToGroupUserAlreadyInvited_Conflict() throws Exception {
		RequestForm form = new RequestForm(GROUP_1_ID, USER_2_USERNAME);
		requestRepository.save(new Request(groupRepository.getReferenceById(GROUP_1_ID),
				userRepository.findByUsername(USER_2_USERNAME)));
		ObjectMapper obj = new ObjectMapper();

		String resultString = mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form))).andExpect(status().isConflict()).andReturn().getResponse()
				.getContentAsString();

		ApiResponse<Request> result = null;
		TypeReference<ApiResponse<Request>> typeReference = new TypeReference<ApiResponse<Request>>() {
		};

		try {
			result = obj.readValue(resultString, typeReference);
		} catch (Exception e) {
			assertTrue(false, "Error parsing response");
		}
		String errorCode = result.getErrorCode();
		assertEquals(CodeErrors.ALREADY_INVITED_USER, errorCode);

	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_InviteToNonexistentGroupAndNonexistentUser_NotFound() throws Exception {

		RequestForm form1 = new RequestForm(NONEXISTING_ID, USER_3_USERNAME);
		RequestForm form2 = new RequestForm(GROUP_1_ID, NONEXISTING_USER);

		ObjectMapper obj = new ObjectMapper();

		mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form1))).andExpect(status().isNotFound());
		mockMvc.perform(post("/api/group/request")
				.contentType("application/json")
				.content(obj.writeValueAsString(form2))).andExpect(status().isNotFound());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_KickFromGroupUser_Ok() throws Exception {

		Group group = groupRepository.getReferenceById(GROUP_1_ID);
		List<User> newUsers = new ArrayList<>(group.getUsers());
		User user = userRepository.findByUsername(USER_2_USERNAME);
		newUsers.add(user);
		group.setUsers(newUsers);
		groupRepository.save(group);
		mockMvc.perform(delete("/api/group/" + GROUP_1_ID + "/members/" + USER_2_USERNAME)).andExpect(status().isOk());
		group = groupRepository.getReferenceById(GROUP_1_ID);
		assertFalse(group.getUsers().contains(user));
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_KickFromGroupCreator_Forbidden() throws Exception {
		mockMvc.perform(delete("/api/group/" + GROUP_1_ID + "/members/" + USER_1_USERNAME))
				.andExpect(status().isForbidden());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_KickFromGroupNotIncludedUser_NotFound() throws Exception {

		ObjectMapper obj = new ObjectMapper();

		String resultString = mockMvc.perform(delete("/api/group/" + GROUP_1_ID + "/members/" + USER_2_USERNAME))
				.andExpect(status().isNotFound())
				.andReturn().getResponse()
				.getContentAsString();
		ApiResponse<Void> result = null;
		TypeReference<ApiResponse<Void>> typeReference = new TypeReference<ApiResponse<Void>>() {
		};

		try {
			result = obj.readValue(resultString, typeReference);
		} catch (Exception e) {
			assertTrue(false, "Error parsing response");
		}
		String errorCode = result.getErrorCode();
		assertEquals(CodeErrors.NOT_GROUP_MEMBER, errorCode);
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_KickFromGroupNonexistentUserAndGroup_NotFound() throws Exception {

		mockMvc.perform(delete("/api/group/" + NONEXISTING_ID + "/members/" + USER_2_USERNAME))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/group/" + GROUP_1_ID + "/members/" + NONEXISTING_USER))
				.andExpect(status().isNotFound());
	}

	@WithMockUser(username = USER_2_USERNAME)
	@Test
	void When_AcceptRequest_Ok() throws Exception {
		Group group = groupRepository.getReferenceById(GROUP_1_ID);
		User user = userRepository.findByUsername(USER_2_USERNAME);
		Request request = new Request(group,
				user);
		request = requestRepository.save(request);

		mockMvc.perform(post("/api/group/request/" + request.getId() + "?accept=true")).andExpect(status().isOk());
		group = groupRepository.getReferenceById(GROUP_1_ID);
		assertTrue(group.getUsers().contains(user));
		assertFalse(requestRepository.findById(GROUP_1_ID).isPresent());
	}

	@WithMockUser(username = USER_2_USERNAME)
	@Test
	void When_RejectRequest_Ok() throws Exception {
		Group group = groupRepository.getReferenceById(GROUP_1_ID);
		User user = userRepository.findByUsername(USER_2_USERNAME);
		Request request = new Request(group,
				user);
		request = requestRepository.save(request);

		mockMvc.perform(post("/api/group/request/" + request.getId() + "?accept=false")).andExpect(status().isOk());
		group = groupRepository.getReferenceById(GROUP_1_ID);
		assertFalse(group.getUsers().contains(user));
		assertFalse(requestRepository.findById(GROUP_1_ID).isPresent());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_RespondRequestOtherUser_Forbidden() throws Exception {
		Group group = groupRepository.getReferenceById(GROUP_1_ID);
		User user = userRepository.findByUsername(USER_2_USERNAME);
		Request request = new Request(group,
				user);
		request = requestRepository.save(request);

		mockMvc.perform(post("/api/group/request/" + request.getId() + "?accept=false"))
				.andExpect(status().isForbidden());
	}

	@WithMockUser(username = USER_1_USERNAME)
	@Test
	void When_RespondNonExistentRequest_NotFound() throws Exception {
		mockMvc.perform(post("/api/group/request/" + NONEXISTING_ID + "?accept=false"))
				.andExpect(status().isNotFound());
	}

}
