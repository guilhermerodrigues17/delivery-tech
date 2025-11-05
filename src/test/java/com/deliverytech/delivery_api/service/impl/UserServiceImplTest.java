package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.LoginRequestDto;
import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.dto.response.LoginResponseDto;
import com.deliverytech.delivery_api.dto.response.RegisterResponseDto;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.User;
import com.deliverytech.delivery_api.model.enums.Role;
import com.deliverytech.delivery_api.repository.UserRepository;
import com.deliverytech.delivery_api.security.TokenService;
import com.deliverytech.delivery_api.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Nested
    @DisplayName("createUser() tests")
    class CreateUserTests {

        private RegisterUserRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = new RegisterUserRequestDto();
            requestDto.setName("Test User");
            requestDto.setEmail("test@email.com");
            requestDto.setPassword("strongpassword123");
            requestDto.setRole(Role.CUSTOMER);
            requestDto.setRestaurantId(null);
        }

        @Test
        @DisplayName("Should throw ConflictException when email already exists")
        void should_ThrowConflictException_When_EmailAlreadyExists() {
            when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

            ConflictException exception = assertThrows(ConflictException.class, () -> {
                userServiceImpl.createUser(requestDto);
            });

            assertEquals("E-mail já utilizado", exception.getMessage());

            verify(userRepository).existsByEmail(requestDto.getEmail());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should throw BusinessException when Role is RESTAURANT but RestaurantId is null")
        void should_ThrowBusinessException_When_RoleRestaurantAndIdIsNull() {
            requestDto.setRole(Role.RESTAURANT);
            requestDto.setRestaurantId(null);

            when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userServiceImpl.createUser(requestDto);
            });

            assertEquals("Para usuários com a role 'RESTAURANT' o ID do restaurante é obrigatório", exception.getMessage());

            verify(userRepository).existsByEmail(requestDto.getEmail());
            verify(restaurantService, never()).findById(any(UUID.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when RestaurantId is provided but not found")
        void should_ThrowResourceNotFound_When_RestaurantIdIsProvidedButNotFound() {
            UUID nonExistentRestaurantId = UUID.randomUUID();
            requestDto.setRole(Role.RESTAURANT);
            requestDto.setRestaurantId(nonExistentRestaurantId);

            when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
            when(restaurantService.findById(nonExistentRestaurantId)).thenThrow(new ResourceNotFoundException("Restaurante não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                userServiceImpl.createUser(requestDto);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());

            verify(userRepository).existsByEmail(requestDto.getEmail());
            verify(restaurantService).findById(nonExistentRestaurantId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should create CUSTOMER successfully")
        void should_CreateCustomer_Successfully() {
            requestDto.setRole(Role.CUSTOMER);

            User savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            savedUser.setName(requestDto.getName());
            savedUser.setEmail(requestDto.getEmail());
            savedUser.setRole(requestDto.getRole());

            when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode("strongpassword123")).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

            RegisterResponseDto response = userServiceImpl.createUser(requestDto);

            assertNotNull(response);
            assertEquals(savedUser.getId(), response.id());
            assertEquals(savedUser.getEmail(), response.email());
            assertEquals(Role.CUSTOMER, response.role());

            verify(userRepository).existsByEmail("test@email.com");
            verify(passwordEncoder).encode(passwordCaptor.capture());

            assertEquals("strongpassword123", passwordCaptor.getValue());

            verify(restaurantService, never()).findById(any(UUID.class));
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertEquals(savedUser.getName(), capturedUser.getName());
            assertEquals(savedUser.getEmail(), capturedUser.getEmail());
            assertEquals("encodedPassword123", capturedUser.getPassword());
            assertEquals(Role.CUSTOMER, capturedUser.getRole());

            assertTrue(capturedUser.getActive());
            assertNull(capturedUser.getRestaurant(), "Restaurant should be null for a CUSTOMER");
        }

        @Test
        @DisplayName("Should create RESTAURANT successfully and link restaurant")
        void should_CreateRestaurantUser_Successfully() {
            UUID restaurantId = UUID.randomUUID();
            requestDto.setRole(Role.RESTAURANT);
            requestDto.setRestaurantId(restaurantId);

            Restaurant foundRestaurant = new Restaurant();
            foundRestaurant.setId(restaurantId);
            foundRestaurant.setName("Test Restaurant");

            User savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            savedUser.setName(requestDto.getName());
            savedUser.setEmail(requestDto.getEmail());
            savedUser.setRole(requestDto.getRole());
            savedUser.setRestaurant(foundRestaurant);

            when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode("strongpassword123")).thenReturn("encodedPassword123");
            when(restaurantService.findById(restaurantId)).thenReturn(foundRestaurant);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            RegisterResponseDto response = userServiceImpl.createUser(requestDto);

            assertNotNull(response);
            assertEquals(savedUser.getId(), response.id());
            assertEquals(Role.RESTAURANT, response.role());

            verify(userRepository).existsByEmail(requestDto.getEmail());
            verify(passwordEncoder).encode("strongpassword123");
            verify(restaurantService).findById(restaurantId);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertEquals(savedUser.getName(), capturedUser.getName());
            assertEquals(Role.RESTAURANT, capturedUser.getRole());
            assertTrue(capturedUser.getActive());
            assertNotNull(capturedUser.getRestaurant(), "Restaurant should not be null");
            assertEquals(restaurantId, capturedUser.getRestaurant().getId());
        }
    }

    @Nested
    @DisplayName("login() tests")
    class LoginTests {

        private LoginRequestDto loginDto;

        @BeforeEach
        void setUp() {
            loginDto = new LoginRequestDto();
            loginDto.setEmail("user@email.com");
            loginDto.setPassword("password123");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when authentication fails")
        void should_ThrowAuthenticationException_When_CredentialsAreInvalid() {
            var authToken = new UsernamePasswordAuthenticationToken(
                    loginDto.getEmail(),
                    loginDto.getPassword()
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> {
                userServiceImpl.login(loginDto);
            });

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("Should return LoginResponseDto with token when authentication succeeds")
        void should_ReturnLoginResponseDto_When_AuthenticationSucceeds() {
            User authenticatedUser = new User();
            authenticatedUser.setId(UUID.randomUUID());
            authenticatedUser.setEmail(loginDto.getEmail());
            authenticatedUser.setRole(Role.CUSTOMER);

            Authentication authentication = mock(Authentication.class);

            String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authenticatedUser);
            when(tokenService.generateToken(authenticatedUser)).thenReturn(fakeToken);

            LoginResponseDto response = userServiceImpl.login(loginDto);

            assertNotNull(response);
            assertEquals(fakeToken, response.token());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(authentication).getPrincipal();
            verify(tokenService).generateToken(authenticatedUser);
        }
    }

    @Nested
    @DisplayName("findById() tests")
    class FindByIdTests {

        private UUID userId;
        private String userIdString;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            userIdString = userId.toString();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdIsInvalidUUID() {
            String invalidUuidString = "not-a-uuid";

            assertThrows(IllegalArgumentException.class, () -> {
                userServiceImpl.findById(invalidUuidString);
            });

            verify(userRepository, never()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID is not found")
        void should_ThrowResourceNotFound_When_IdNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                userServiceImpl.findById(userIdString);
            });

            assertEquals("Recurso não encontrado", exception.getMessage());

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should return User when ID is found")
        void should_ReturnUser_When_IdIsFound() {
            User expectedUser = new User();
            expectedUser.setId(userId);
            expectedUser.setEmail("found@email.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

            User actualUser = userServiceImpl.findById(userIdString);

            assertNotNull(actualUser);
            assertEquals(expectedUser, actualUser);
            assertEquals(userId, actualUser.getId());

            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("findByEmail() tests")
    class FindByEmailTests {

        private String testEmail;

        @BeforeEach
        void setUp() {
            testEmail = "test@email.com";
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when email is not found")
        void should_ThrowResourceNotFound_When_EmailNotFound() {
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                userServiceImpl.findByEmail(testEmail);
            });

            assertEquals("Recurso não encontrado", exception.getMessage());

            verify(userRepository).findByEmail(testEmail);
        }

        @Test
        @DisplayName("Should return User when email is found")
        void should_ReturnUser_When_EmailIsFound() {
            User expectedUser = new User();
            expectedUser.setId(UUID.randomUUID());
            expectedUser.setEmail(testEmail);

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(expectedUser));

            User actualUser = userServiceImpl.findByEmail(testEmail);

            assertNotNull(actualUser);
            assertEquals(expectedUser, actualUser);
            assertEquals(testEmail, actualUser.getEmail());

            verify(userRepository).findByEmail(testEmail);
        }
    }
}