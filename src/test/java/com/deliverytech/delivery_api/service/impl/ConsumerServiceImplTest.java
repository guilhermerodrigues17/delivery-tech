package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.ConsumerMapper;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.repository.ConsumerRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsumerServiceImplTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private ConsumerMapper mapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Nested
    @DisplayName("create() tests")
    class ConsumerCreateTests {
        @Test
        @DisplayName("Should throw conflict exception when email already exists")
        void should_ThrowConflictException_When_EmailAlreadyExists() {
            ConsumerRequestDto requestDto = new ConsumerRequestDto();
            requestDto.setEmail("duplicatedEmail@email.com");
            requestDto.setName("Consumer name");

            when(consumerRepository.existsByEmail("duplicatedEmail@email.com")).thenReturn(true);

            ConflictException exception = assertThrows(ConflictException.class, () -> {
                consumerService.create(requestDto);
            });

            assertEquals("E-mail já está em uso", exception.getMessage());
            verify(consumerRepository).existsByEmail("duplicatedEmail@email.com");
            verify(consumerRepository, never()).save(any(Consumer.class));
        }

        @Test
        @DisplayName("Should create consumer with success when data is valid")
        void should_CreateConsumer_When_DataIsValid() {
            ConsumerRequestDto requestDto = new ConsumerRequestDto();
            requestDto.setEmail("consumer@email.com");
            requestDto.setName("Consumer");
            requestDto.setAddress("Rua A, 1000");
            requestDto.setPhoneNumber("11922334455");

            Consumer consumerToSave = new Consumer();
            consumerToSave.setEmail("consumer@email.com");
            consumerToSave.setName("Consumer");

            Consumer savedConsumer = new Consumer();
            savedConsumer.setId(UUID.randomUUID());
            savedConsumer.setName(consumerToSave.getName());
            savedConsumer.setEmail(consumerToSave.getEmail());
            savedConsumer.setActive(true);

            ConsumerResponseDto expectedResponse = new ConsumerResponseDto(
                    savedConsumer.getId(),
                    savedConsumer.getName(),
                    savedConsumer.getEmail(),
                    null,
                    null,
                    savedConsumer.getActive()
            );

            when(consumerRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
            when(mapper.toEntity(requestDto)).thenReturn(consumerToSave);
            when(consumerRepository.save(any(Consumer.class))).thenReturn(savedConsumer);
            when(mapper.toDto(savedConsumer)).thenReturn(expectedResponse);

            ConsumerResponseDto createResponse = consumerService.create(requestDto);

            assertNotNull(createResponse);

            assertEquals(expectedResponse.id(), createResponse.id());
            assertEquals(expectedResponse.email(), createResponse.email());
            assertEquals(expectedResponse.active(), createResponse.active());

            verify(consumerRepository).existsByEmail("consumer@email.com");
            verify(mapper).toEntity(requestDto);
            verify(consumerRepository).save(consumerToSave);
            verify(mapper).toDto(savedConsumer);
        }
    }

    @Nested
    @DisplayName("findById() and findByIdResponse() tests")
    class ConsumerFindByIdTests {
        @Test
        @DisplayName("Should throw NotFound exception when id not exists")
        void shouldThrowResourceNotFoundExceptionWhenIdNotExists() {
            var uuid = UUID.randomUUID();

            when(consumerRepository.findById(uuid)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                consumerService.findById(uuid);
            });

            assertEquals("Cliente não encontrado", exception.getMessage());
            verify(consumerRepository).findById(uuid);
            verify(mapper, never()).toDto(any(Consumer.class));
        }

        @Test
        @DisplayName("Should return a ConsumerResponseDto when id exists")
        void shouldReturnConsumerResponseDtoWhenIdExists() {
            final UUID uuid = UUID.randomUUID();

            Consumer consumerFound = new Consumer();
            consumerFound.setId(uuid);
            consumerFound.setName("Consumer");
            consumerFound.setEmail("consumer@email.com");
            consumerFound.setActive(true);

            ConsumerResponseDto responseDto = new ConsumerResponseDto(
                    uuid,
                    consumerFound.getName(),
                    consumerFound.getEmail(),
                    null,
                    null,
                    consumerFound.getActive()
            );

            when(consumerRepository.findById(uuid)).thenReturn(Optional.of(consumerFound));
            when(mapper.toDto(consumerFound)).thenReturn(responseDto);

            ConsumerResponseDto expectedResult = consumerService.findByIdResponse(uuid.toString());

            assertNotNull(expectedResult);
            assertEquals(responseDto.id(), expectedResult.id());
            assertEquals(responseDto.name(), expectedResult.name());

            verify(consumerRepository).findById(uuid);
            verify(mapper).toDto(consumerFound);
        }
    }

    @Nested
    @DisplayName("findAllActive() tests")
    class findAllActiveTests {
        @Test
        @DisplayName("Should return all active consumers with pagination when data exists")
        void should_ReturnAllActiveConsumers_When_DataExists() {
            Pageable pageable = PageRequest.of(0, 10);

            Consumer c1 = new Consumer();
            c1.setId(UUID.randomUUID());
            c1.setName("Consumer 1");

            Consumer c2 = new Consumer();
            c2.setId(UUID.randomUUID());
            c2.setName("Consumer 2");

            List<Consumer> consumersList = List.of(c1, c2);
            Page<Consumer> consumersPageMock = new PageImpl<>(consumersList, pageable, consumersList.size());

            var dtoC1 = new ConsumerResponseDto(c1.getId(), c1.getName(), null, null, null, true);
            var dtoC2 = new ConsumerResponseDto(c2.getId(), c2.getName(), null, null, null, true);

            when(consumerRepository.findByActiveTrue(pageable)).thenReturn(consumersPageMock);
            when(mapper.toDto(c1)).thenReturn(dtoC1);
            when(mapper.toDto(c2)).thenReturn(dtoC2);

            Page<ConsumerResponseDto> result = consumerService.findAllActive(pageable);

            assertNotNull(result);

            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getTotalPages());

            assertEquals(2, result.getContent().size());
            assertEquals("Consumer 1", result.getContent().get(0).name());
            assertEquals("Consumer 2", result.getContent().get(1).name());

            verify(consumerRepository).findByActiveTrue(pageable);
            verify(mapper).toDto(c1);
            verify(mapper).toDto(c2);
            verify(mapper, times(2)).toDto(any(Consumer.class));
        }

        @Test
        @DisplayName("Should return an empty page when no data exists")
        void should_ReturnEmptyPage_When_NoDataExists() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Consumer> consumersPageMock = Page.empty(pageable);

            when(consumerRepository.findByActiveTrue(pageable)).thenReturn(consumersPageMock);

            Page<ConsumerResponseDto> result = consumerService.findAllActive(pageable);

            assertNotNull(result);

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());

            verify(consumerRepository).findByActiveTrue(pageable);
            verify(mapper, never()).toDto(any(Consumer.class));
        }
    }

    @Nested
    @DisplayName("updateConsumer() tests")
    class UpdateConsumerTests {
        private UUID consumerId;
        private Consumer existingConsumer;
        private ConsumerRequestDto updateRequest;

        @BeforeEach
        void setup() {
            consumerId = UUID.randomUUID();

            updateRequest = new ConsumerRequestDto();
            updateRequest.setName("Update Consumer");
            updateRequest.setEmail("new@email.com");
            updateRequest.setAddress("Rua A, 2000");
            updateRequest.setPhoneNumber("(11) 92233-4455");

            existingConsumer = new Consumer();
            existingConsumer.setId(consumerId);
            existingConsumer.setName("Consumer");
            existingConsumer.setEmail("actual@email.com");
            existingConsumer.setAddress("Rua A, 1000");
            existingConsumer.setPhoneNumber("11966778899");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when consumer not exists")
        void shouldThrowResourceNotFoundExceptionWhenConsumerNotExists() {
            when(consumerRepository.findById(consumerId)).thenReturn(Optional.empty());

            var exception = assertThrows(ResourceNotFoundException.class, () -> {
                consumerService.updateConsumer(consumerId.toString(), updateRequest);
            });

            assertEquals("Cliente não encontrado", exception.getMessage());

            verify(consumerRepository).findById(consumerId);
            verify(consumerRepository, never()).existsByEmail(anyString());
            verify(consumerRepository, never()).save(any(Consumer.class));
        }

        @Test
        @DisplayName("Should throw ConflictException when new email already exists")
        void shouldThrowConflictExceptionWhenNewEmailAlreadyExists() {
            when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(existingConsumer));
            when(consumerRepository.existsByEmail("new@email.com")).thenReturn(true);

            var exception = assertThrows(ConflictException.class, () -> {
                consumerService.updateConsumer(consumerId.toString(), updateRequest);
            });

            assertEquals("E-mail já está em uso", exception.getMessage());

            verify(consumerRepository).findById(consumerId);
            verify(consumerRepository).existsByEmail("new@email.com");
            verify(consumerRepository, never()).save(any(Consumer.class));
        }

        @Test
        @DisplayName("Should update consumer when data is valid")
        void shouldUpdateConsumerWhenDataIsValid() {
            when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(existingConsumer));
            when(consumerRepository.existsByEmail("new@email.com")).thenReturn(false);

            when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            when(mapper.toDto(any(Consumer.class))).thenAnswer(invocation -> {
                Consumer updatedConsumer = invocation.getArgument(0);
                return new ConsumerResponseDto(
                        updatedConsumer.getId(),
                        updatedConsumer.getName(),
                        updatedConsumer.getEmail(),
                        updatedConsumer.getPhoneNumber(),
                        updatedConsumer.getAddress(),
                        updatedConsumer.getActive()
                );
            });

            ConsumerResponseDto expectedResponse = consumerService.updateConsumer(consumerId.toString(), updateRequest);

            assertNotNull(expectedResponse);

            assertEquals(updateRequest.getName(), expectedResponse.name());
            assertEquals(updateRequest.getEmail(), expectedResponse.email());
            assertEquals(updateRequest.getAddress(), expectedResponse.address());
            assertEquals("11922334455", expectedResponse.phoneNumber());

            verify(consumerRepository).findById(consumerId);
            verify(consumerRepository).existsByEmail(updateRequest.getEmail());
            verify(consumerRepository).save(any(Consumer.class));
            verify(mapper).toDto(any(Consumer.class));
        }

        @Test
        @DisplayName("Should update consumer without email check when email is unchanged")
        void shouldUpdateConsumerWhenEmailIsUnchanged() {
            updateRequest.setEmail("actual@email.com");

            when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(existingConsumer));

            when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            when(mapper.toDto(any(Consumer.class))).thenAnswer(invocation -> {
                Consumer updatedConsumer = invocation.getArgument(0);
                return new ConsumerResponseDto(
                        updatedConsumer.getId(),
                        updatedConsumer.getName(),
                        updatedConsumer.getEmail(),
                        updatedConsumer.getPhoneNumber(),
                        updatedConsumer.getAddress(),
                        updatedConsumer.getActive()
                );
            });

            ConsumerResponseDto expectedResponse = consumerService.updateConsumer(consumerId.toString(), updateRequest);

            assertNotNull(expectedResponse);

            assertEquals(updateRequest.getName(), expectedResponse.name());
            assertEquals(updateRequest.getEmail(), expectedResponse.email());

            verify(consumerRepository).findById(consumerId);
            verify(consumerRepository, never()).existsByEmail(anyString());
            verify(consumerRepository).save(any(Consumer.class));
            verify(mapper).toDto(any(Consumer.class));
        }
    }
}
