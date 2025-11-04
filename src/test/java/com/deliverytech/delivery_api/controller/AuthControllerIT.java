package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.BaseIntegrationTest;
import com.deliverytech.delivery_api.dto.request.LoginRequestDto;
import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.model.User;
import com.deliverytech.delivery_api.model.enums.ErrorCode;
import com.deliverytech.delivery_api.model.enums.Role;
import com.deliverytech.delivery_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("POST /auth/register tests")
    class RegisterTests {
        private RegisterUserRequestDto userRequest;

        @BeforeEach
        void setUp() {
            userRequest = new RegisterUserRequestDto();
            userRequest.setName("User");
            userRequest.setEmail("user@email.com");
            userRequest.setRole(Role.CUSTOMER);
            userRequest.setPassword("password");
        }

        @Test
        @DisplayName("Should register an user with success when data is valid")
        void should_RegisterUserWithSuccess_When_DataIsValid() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(userRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isCreated())

                    .andExpect(jsonPath("$.data", notNullValue()))
                    .andExpect(jsonPath("$.success", is(true)))

                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.email", is("user@email.com")))
                    .andExpect(jsonPath("$.data.role", is(Role.CUSTOMER.name())));
        }

        @Test
        @DisplayName("Should return 409 - Conflict when e-mail already exists")
        void should_ReturnConflict_When_EmailAlreadyExists() throws Exception {
            User existingUser = new User();
            existingUser.setName("Existing");
            existingUser.setEmail("user@email.com");
            existingUser.setPassword(passwordEncoder.encode("password"));
            existingUser.setActive(true);
            userRepository.save(existingUser);

            String jsonBody = objectMapper.writeValueAsString(userRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isConflict())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.CONFLICT_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.CONFLICT_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when data is not valid")
        void should_ReturnBadRequest_When_DataIsNotValid() throws Exception {
            userRequest.setEmail("user@");
            userRequest.setPassword("");

            String jsonBody = objectMapper.writeValueAsString(userRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 422 - Unprocessable Entity when restaurant user do not provide a restaurantId")
        void should_ReturnBusinessException_When_RestaurantUserDoNotProvideARestaurantId() throws Exception {
            userRequest.setRole(Role.RESTAURANT);

            String jsonBody = objectMapper.writeValueAsString(userRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isUnprocessableEntity())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNPROCESSABLE_ENTITY.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when restaurantId does not exist")
        void should_ReturnResourceNotFound_When_RestaurantIdNotExists() throws Exception {
            userRequest.setRole(Role.RESTAURANT);
            userRequest.setRestaurantId(UUID.randomUUID());

            String jsonBody = objectMapper.writeValueAsString(userRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Nested
    @DisplayName("POST /auth/login tests")
    class LoginTests {

        @Test
        @DisplayName("Should login an user when credentials match")
        void should_LoginUser_When_CredentialsMatch() throws Exception {
            User registeredUser = new User();
            registeredUser.setName("User");
            registeredUser.setEmail("user@email.com");
            registeredUser.setPassword(passwordEncoder.encode("password"));
            registeredUser.setRole(Role.CUSTOMER);
            registeredUser.setActive(true);

            userRepository.save(registeredUser);

            LoginRequestDto loginRequest = new LoginRequestDto();
            loginRequest.setEmail("user@email.com");
            loginRequest.setPassword("password");

            String jsonBody = objectMapper.writeValueAsString(loginRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when data is not valid")
        void should_ReturnBadRequest_When_DataIsNotValid() throws Exception {
            LoginRequestDto loginRequest = new LoginRequestDto();
            loginRequest.setEmail("user@");
            loginRequest.setPassword("password");

            String jsonBody = objectMapper.writeValueAsString(loginRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when credentials do not match")
        void should_ReturnUnauthorized_When_CredentialsDoNotMatch() throws Exception {
            LoginRequestDto loginRequest = new LoginRequestDto();
            loginRequest.setEmail("user@email.com");
            loginRequest.setPassword("password");

            String jsonBody = objectMapper.writeValueAsString(loginRequest);

            ResultActions result = mockMvc.perform(
                    post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );

            result.andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }
}