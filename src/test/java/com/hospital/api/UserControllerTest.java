// src/main/java/com/hospital/api/UserController.java
package com.hospital.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.application.common.exceptions.NotFoundException;
import com.hospital.application.dto.UserCreateRequest;
import com.hospital.application.dto.UserResponse;
import com.hospital.application.service.UserService;
import com.hospital.domain.role.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.hospital.application.common.exceptions.GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired 
    MockMvc mvc;

    @Autowired 
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    void create_returns_user() throws Exception {
        var request = new UserCreateRequest("Ali", Set.of(UserRole.PATIENT));
        var response = new UserResponse(UUID.randomUUID(), "Ali", Set.of(UserRole.PATIENT));
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userID")
                .value(response.userID().toString()))
            .andExpect(jsonPath("$.userName")
                .value("Ali"))
            .andExpect(jsonPath("$.userRoles[0]")
                .value("PATIENT"));
    }

    @Test
    void create_validation_errors_400() throws Exception {
        var invalid = """
        {"userName":"", "userRoles": []}
        """;

        mvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalid))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Validation failed"))
            .andExpect(jsonPath("$.errors.userName")
                .exists())
            .andExpect(jsonPath("$.errors.userRoles")
                .exists());
    }

    @Test
    void list_returns_users() throws Exception {
        var user1 = new UserResponse(UUID.randomUUID(), "A", Set.of(UserRole.DOCTOR));
        var user2 = new UserResponse(UUID.randomUUID(), "B", Set.of(UserRole.PATIENT, UserRole.DOCTOR));

        when(userService.listUsers()).thenReturn(List.of(user1, user2));

        mvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userName")
                .value("A"))
            .andExpect(jsonPath("$[1].userRoles", hasItem("PATIENT")));
    }

    @Test
    void get_returns_user_or_404() throws Exception {
        var id = UUID.randomUUID();
        var user = new UserResponse(id, "X", Set.of(UserRole.PATIENT));

        when(userService.getUser(id)).thenReturn(user);

        mvc.perform(get("/api/users/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userID")
                .value(id.toString()));

        // 404
        var missing = UUID.randomUUID();

        when(userService.getUser(missing)).thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/api/users/{id}", missing))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("User not found"));
    }
}