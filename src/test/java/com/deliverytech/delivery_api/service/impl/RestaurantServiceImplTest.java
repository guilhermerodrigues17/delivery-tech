package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.events.restaurant.RestaurantCreatedEvent;
import com.deliverytech.delivery_api.events.restaurant.RestaurantUpdateEvent;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.validation.RestaurantValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {
    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper mapper;

    @Mock
    private SecurityService securityService;

    @Mock
    private RestaurantValidator restaurantValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @Nested
    @DisplayName("createRestaurant() tests")
    class CreateRestaurantTests {

        private RestaurantRequestDto requestDto;

        @BeforeEach
        void setUp() {
            requestDto = new RestaurantRequestDto();
            requestDto.setName("Test Restaurant");
            requestDto.setCategory("BRASILEIRA");
            requestDto.setAddress("Test Address, 123");
            requestDto.setDeliveryTax(BigDecimal.TEN);
            requestDto.setPhoneNumber("(11) 98765-4321");
        }

        @Test
        @DisplayName("Should throw ConflictException when restaurant name already exists")
        void should_ThrowConflictException_When_NameAlreadyExists() {
            doThrow(new ConflictException("Nome de restaurante já está em uso")).when(restaurantValidator)
                    .validateName(requestDto.getName());

            ConflictException exception = assertThrows(ConflictException.class, () -> {
                restaurantService.createRestaurant(requestDto);
            });

            assertEquals("Nome de restaurante já está em uso", exception.getMessage());

            verify(restaurantValidator, times(1)).validateName(requestDto.getName());
            verify(restaurantRepository, never()).save(any(Restaurant.class));
            verify(mapper, never()).toEntity(any(RestaurantRequestDto.class));
        }

        @Test
        @DisplayName("Should create restaurant, trim phone, and set active when data is valid")
        void should_CreateRestaurant_When_DataIsValid() {
            Restaurant restaurantFromMapper = new Restaurant();

            Restaurant savedRestaurant = new Restaurant();
            savedRestaurant.setId(UUID.randomUUID());
            savedRestaurant.setName(requestDto.getName());
            savedRestaurant.setActive(true);
            savedRestaurant.setPhoneNumber("11987654321");

            RestaurantResponseDto expectedResult = new RestaurantResponseDto(
                    savedRestaurant.getId(),
                    savedRestaurant.getName(),
                    requestDto.getCategory(),
                    savedRestaurant.getPhoneNumber(),
                    requestDto.getAddress(),
                    true,
                    "10.00"
            );

            doNothing().when(restaurantValidator).validateName(requestDto.getName());
            when(mapper.toEntity(requestDto)).thenReturn(restaurantFromMapper);
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(savedRestaurant);
            when(mapper.toDto(savedRestaurant)).thenReturn(expectedResult);
            when(securityService.getCurrentUser()).thenReturn(Optional.empty());
            doNothing().when(eventPublisher).publishEvent(any(RestaurantCreatedEvent.class));

            RestaurantResponseDto result = restaurantService.createRestaurant(requestDto);

            assertNotNull(result);
            assertEquals(expectedResult.id(), result.id());
            assertEquals(expectedResult.name(), result.name());

            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);
            verify(restaurantRepository).save(restaurantCaptor.capture());

            Restaurant capturedRestaurant = restaurantCaptor.getValue();
            assertEquals(expectedResult.phoneNumber(), capturedRestaurant.getPhoneNumber());
            assertEquals(expectedResult.active(), capturedRestaurant.getActive());

            verify(restaurantValidator, times(1)).validateName(requestDto.getName());
            verify(eventPublisher, times(1)).publishEvent(any(RestaurantCreatedEvent.class));
        }
    }

    @Nested
    @DisplayName("updateRestaurant() tests")
    class UpdateRestaurantTests {

        private UUID restaurantId;
        private RestaurantRequestDto updateDto;
        private Restaurant existingRestaurant;

        @BeforeEach
        void setUp() {
            restaurantId = UUID.randomUUID();

            updateDto = new RestaurantRequestDto();
            updateDto.setName("New Restaurant Name");
            updateDto.setCategory("ITALIANA");
            updateDto.setAddress("New Address, 456");
            updateDto.setDeliveryTax(new BigDecimal("12.00"));
            updateDto.setPhoneNumber("(11) 91111-2222");

            existingRestaurant = new Restaurant();
            existingRestaurant.setId(restaurantId);
            existingRestaurant.setName("Old Restaurant Name");
            existingRestaurant.setCategory("BRASILEIRA");
            existingRestaurant.setAddress("Old Address, 123");
            existingRestaurant.setDeliveryTax(BigDecimal.TEN);
            existingRestaurant.setPhoneNumber("11900000000");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurant to update does not exist")
        void should_ThrowResourceNotFound_When_RestaurantNotFound() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                restaurantService.updateRestaurant(restaurantId.toString(), updateDto);
            });

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository, never()).existsByName(anyString());
            verify(restaurantRepository, never()).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should throw ConflictException when new name is already in use")
        void should_ThrowConflictException_When_NewNameIsDuplicated() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
            doThrow(new ConflictException("Nome de restaurante já está em uso")).when(restaurantValidator)
                    .validateName(updateDto.getName());

            ConflictException exception = assertThrows(ConflictException.class, () -> {
                restaurantService.updateRestaurant(restaurantId.toString(), updateDto);
            });

            assertEquals("Nome de restaurante já está em uso", exception.getMessage());

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantValidator, times(1)).validateName(updateDto.getName());
            verify(restaurantRepository, never()).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should update successfully and NOT check for name conflict if name is unchanged")
        void should_UpdateSuccessfully_When_NameIsUnchanged() {
            updateDto.setName("Old Restaurant Name");
            updateDto.setAddress("Address Updated");

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toDto(any(Restaurant.class))).thenAnswer(inv -> {
                Restaurant mutated = inv.getArgument(0);
                return new RestaurantResponseDto(
                        mutated.getId(), mutated.getName(), mutated.getCategory(),
                        mutated.getPhoneNumber(), mutated.getAddress(), mutated.getActive(),
                        mutated.getDeliveryTax().toString()
                );
            });

            RestaurantResponseDto result = restaurantService.updateRestaurant(restaurantId.toString(), updateDto);

            assertEquals("Address Updated", result.address());

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository, never()).existsByName(anyString());
            verify(restaurantRepository).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should update successfully when name changes")
        void should_UpdateSuccessfully_When_NameIsChanged() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
            doNothing().when(restaurantValidator).validateName(updateDto.getName());

            when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.toDto(any(Restaurant.class))).thenAnswer(inv -> {
                Restaurant mutated = inv.getArgument(0);
                return new RestaurantResponseDto(
                        mutated.getId(), mutated.getName(), mutated.getCategory(),
                        mutated.getPhoneNumber(), mutated.getAddress(), mutated.getActive(),
                        mutated.getDeliveryTax().toString()
                );
            });

            when(securityService.getCurrentUser()).thenReturn(Optional.empty());
            doNothing().when(eventPublisher).publishEvent(any(RestaurantUpdateEvent.class));

            RestaurantResponseDto result = restaurantService.updateRestaurant(restaurantId.toString(), updateDto);

            assertEquals(updateDto.getName(), result.name());
            assertEquals(updateDto.getCategory(), result.category());
            assertEquals(updateDto.getAddress(), result.address());

            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);
            verify(restaurantRepository).save(restaurantCaptor.capture());

            Restaurant capturedRestaurant = restaurantCaptor.getValue();
            assertEquals("11911112222", capturedRestaurant.getPhoneNumber());
            assertEquals(updateDto.getName(), capturedRestaurant.getName());

            verify(restaurantValidator, times(1)).validateName(updateDto.getName());
            verify(eventPublisher, times(1)).publishEvent(any(RestaurantUpdateEvent.class));
        }
    }

    @Nested
    @DisplayName("calculateDeliveryTax() tests")
    class CalculateDeliveryTaxTests {

        private UUID restaurantId;
        private Restaurant mockRestaurant;

        @BeforeEach
        void setUp() {
            restaurantId = UUID.randomUUID();

            mockRestaurant = new Restaurant();
            mockRestaurant.setId(restaurantId);
            mockRestaurant.setDeliveryTax(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurant does not exist")
        void should_ThrowResourceNotFound_When_RestaurantNotFound() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                restaurantService.calculateDeliveryTax(restaurantId.toString(), "06401-000");
            });

            verify(restaurantRepository).findById(restaurantId);
        }

        @Test
        @DisplayName("Should throw BusinessException when CEP is not in any defined zone")
        void should_ThrowBusinessException_When_CepZoneIsNotFound() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));

            String invalidCep = "99999-999";

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                restaurantService.calculateDeliveryTax(restaurantId.toString(), invalidCep);
            });

            assertEquals(
                    "Desculpe, este restaurante não realiza entregas para o CEP informado.",
                    exception.getMessage()
            );

            verify(restaurantRepository).findById(restaurantId);
        }

        @Test
        @DisplayName("Should return base tax for SHORT_DISTANCE zone")
        void should_ReturnBaseTax_When_ZoneIsShortDistance() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
            String cep = "06401-000";

            BigDecimal result = restaurantService.calculateDeliveryTax(restaurantId.toString(), cep);

            assertEquals(0, result.compareTo(new BigDecimal("10.00")));
        }

        @Test
        @DisplayName("Should return base tax + 5.00 for MEDIUM_DISTANCE zone")
        void should_ReturnBaseTaxPlus5_When_ZoneIsMediumDistance() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
            String cep = "06451-000";

            BigDecimal deliveryTax = restaurantService.calculateDeliveryTax(restaurantId.toString(), cep);

            assertEquals(0, deliveryTax.compareTo(new BigDecimal("15.00")));
        }

        @Test
        @DisplayName("Should return base tax + 10.00 for LONG_DISTANCE zone")
        void should_ReturnBaseTaxPlus10_When_ZoneIsLongDistance() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(mockRestaurant));
            String cep = "06471-000";

            BigDecimal deliveryTax = restaurantService.calculateDeliveryTax(restaurantId.toString(), cep);

            assertEquals(0, deliveryTax.compareTo(new BigDecimal("20.00")));
        }
    }

    @Nested
    @DisplayName("searchRestaurants() tests")
    class SearchRestaurantsTests {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);
        }

        @Test
        @DisplayName("Should build correct Example probe and map results when all parameters are provided")
        void should_BuildExampleAndMapResults_When_AllParametersProvided() {
            String name = "Burger";
            String category = "FAST_FOOD";
            Boolean active = true;

            Restaurant foundRestaurant = new Restaurant();
            foundRestaurant.setId(UUID.randomUUID());
            foundRestaurant.setName("Test Burger");
            List<Restaurant> restaurantList = List.of(foundRestaurant);
            Page<Restaurant> mockRepoPage = new PageImpl<>(restaurantList, pageable, 1);

            RestaurantResponseDto mappedDto = new RestaurantResponseDto(
                    foundRestaurant.getId(), "Test Burger", null, null, null, null,  null
            );

            when(restaurantRepository.findAll(any(Example.class), eq(pageable)))
                    .thenReturn(mockRepoPage);

            when(mapper.toDto(foundRestaurant)).thenReturn(mappedDto);

            ArgumentCaptor<Example<Restaurant>> exampleCaptor = ArgumentCaptor.forClass(Example.class);

            Page<RestaurantResponseDto> resultPage = restaurantService
                    .searchRestaurants(name, category, active, pageable);

            assertNotNull(resultPage);

            assertEquals(1, resultPage.getTotalElements());
            assertEquals("Test Burger", resultPage.getContent().get(0).name());

            verify(restaurantRepository).findAll(exampleCaptor.capture(), eq(pageable));

            Example<Restaurant> capturedExample = exampleCaptor.getValue();
            assertEquals("Burger", capturedExample.getProbe().getName());
            assertEquals("FAST_FOOD", capturedExample.getProbe().getCategory());
            assertEquals(true, capturedExample.getProbe().getActive());

            verify(mapper).toDto(foundRestaurant);
        }

        @Test
        @DisplayName("Should build correct Example probe when some parameters are null")
        void should_BuildExample_When_SomeParametersAreNull() {
            String name = "Test";
            Boolean active = true;

            Page<Restaurant> mockRepoPage = Page.empty(pageable);

            when(restaurantRepository.findAll(any(Example.class), eq(pageable)))
                    .thenReturn(mockRepoPage);

            ArgumentCaptor<Example<Restaurant>> exampleCaptor = ArgumentCaptor.forClass(Example.class);

            Page<RestaurantResponseDto> resultPage = restaurantService
                    .searchRestaurants(name, null, active, pageable);

            assertTrue(resultPage.isEmpty());

            verify(restaurantRepository).findAll(exampleCaptor.capture(), eq(pageable));
            Example<Restaurant> capturedExample = exampleCaptor.getValue();

            assertEquals("Test", capturedExample.getProbe().getName());
            assertNull(capturedExample.getProbe().getCategory(), "Category should be null in the probe");
            assertEquals(true, capturedExample.getProbe().getActive());

            verify(mapper, never()).toDto(any(Restaurant.class));
        }
    }

    @Nested
    @DisplayName("updateStatusActive() tests")
    class UpdateStatusActiveTests {

        private UUID restaurantId;
        private Restaurant existingRestaurant;
        private RestaurantStatusUpdateDto statusDto;

        @BeforeEach
        void setUp() {
            restaurantId = UUID.randomUUID();

            existingRestaurant = new Restaurant();
            existingRestaurant.setId(restaurantId);

            statusDto = new RestaurantStatusUpdateDto();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurant does not exist")
        void should_ThrowResourceNotFound_When_RestaurantNotFound() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            statusDto.setActive(true);

            assertThrows(ResourceNotFoundException.class, () -> {
                restaurantService.updateStatusActive(restaurantId.toString(), statusDto);
            });

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository, never()).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should update active status from true to false")
        void should_UpdateStatus_FromTrueToFalse() {
            existingRestaurant.setActive(true);
            statusDto.setActive(false);

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(null);

            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);

            assertDoesNotThrow(() -> {
                restaurantService.updateStatusActive(restaurantId.toString(), statusDto);
            });

            verify(restaurantRepository).save(restaurantCaptor.capture());

            Restaurant savedRestaurant = restaurantCaptor.getValue();
            assertFalse(savedRestaurant.getActive(), "Restaurant 'active' flag should be false");
        }

        @Test
        @DisplayName("Should update active status from false to true")
        void should_UpdateStatus_FromFalseToTrue() {
            existingRestaurant.setActive(false);
            statusDto.setActive(true);

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(null);

            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);

            assertDoesNotThrow(() -> {
                restaurantService.updateStatusActive(restaurantId.toString(), statusDto);
            });

            verify(restaurantRepository).save(restaurantCaptor.capture());

            Restaurant savedRestaurant = restaurantCaptor.getValue();
            assertTrue(savedRestaurant.getActive(), "Restaurant 'active' flag should be true");
        }
    }

    @Nested
    @DisplayName("findAllActive() tests")
    class FindAllActiveTests {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);
        }

        @Test
        @DisplayName("Should return a paged list of DTOs when active restaurants are found")
        void should_ReturnDtoPage_When_ActiveRestaurantsAreFound() {
            Restaurant rest1 = new Restaurant();
            rest1.setId(UUID.randomUUID());
            rest1.setName("Active Rest 1");
            rest1.setActive(true);

            List<Restaurant> restaurantList = List.of(rest1);
            Page<Restaurant> mockRepoPage = new PageImpl<>(restaurantList, pageable, 1);

            RestaurantResponseDto dto1 = new RestaurantResponseDto(
                    rest1.getId(), rest1.getName(), null, null, null, null, null
            );

            when(restaurantRepository.findByActiveTrue(pageable)).thenReturn(mockRepoPage);
            when(mapper.toDto(rest1)).thenReturn(dto1);

            Page<RestaurantResponseDto> resultPage = restaurantService.findAllActive(pageable);

            assertNotNull(resultPage);
            assertEquals(1, resultPage.getTotalElements());
            assertEquals(1, resultPage.getContent().size());
            assertEquals("Active Rest 1", resultPage.getContent().get(0).name());

            verify(restaurantRepository).findByActiveTrue(pageable);
            verify(mapper).toDto(rest1);
        }

        @Test
        @DisplayName("Should return an empty page when no active restaurants are found")
        void should_ReturnEmptyPage_When_NoActiveRestaurantsFound() {
            Page<Restaurant> emptyRepoPage = Page.empty(pageable);

            when(restaurantRepository.findByActiveTrue(pageable)).thenReturn(emptyRepoPage);

            Page<RestaurantResponseDto> resultPage = restaurantService.findAllActive(pageable);

            assertNotNull(resultPage);
            assertTrue(resultPage.isEmpty());
            assertEquals(0, resultPage.getTotalElements());

            verify(restaurantRepository).findByActiveTrue(pageable);
            verify(mapper, never()).toDto(any(Restaurant.class));
        }
    }

    @Nested
    @DisplayName("findByIdResponse() tests")
    class FindByIdResponseTests {

        private UUID restaurantId;
        private String restaurantIdString;

        @BeforeEach
        void setUp() {
            restaurantId = UUID.randomUUID();
            restaurantIdString = restaurantId.toString();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdIsInvalidUUID() {
            String invalidUuidString = "not-a-real-uuid";

            assertThrows(IllegalArgumentException.class, () -> {
                restaurantService.findByIdResponse(invalidUuidString);
            });

            verify(restaurantRepository, never()).findById(any(UUID.class));
            verify(mapper, never()).toDto(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurant ID does not exist")
        void should_ThrowResourceNotFound_When_IdDoesNotExist() {
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                restaurantService.findByIdResponse(restaurantIdString);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());


            verify(restaurantRepository).findById(restaurantId);
            verify(mapper, never()).toDto(any(Restaurant.class));
        }

        @Test
        @DisplayName("Should return RestaurantResponseDto when ID exists")
        void should_ReturnDto_When_IdExists() {
            Restaurant foundRestaurant = new Restaurant();
            foundRestaurant.setId(restaurantId);
            foundRestaurant.setName("Found Restaurant");

            RestaurantResponseDto expectedDto = new RestaurantResponseDto(
                    restaurantId, "Found Restaurant", null, null, null, null, null
            );

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(foundRestaurant));
            when(mapper.toDto(foundRestaurant)).thenReturn(expectedDto);

            RestaurantResponseDto actualDto = restaurantService.findByIdResponse(restaurantIdString);

            assertNotNull(actualDto);
            assertEquals(expectedDto, actualDto);
            assertEquals(restaurantIdString, actualDto.id().toString());

            verify(restaurantRepository).findById(restaurantId);
            verify(mapper).toDto(foundRestaurant);
        }
    }

    @Nested
    @DisplayName("isOwner() tests")
    class IsOwnerTests {

        private UUID ownerRestaurantId;
        private String ownerRestaurantIdString;

        @BeforeEach
        void setUp() {
            ownerRestaurantId = UUID.randomUUID();
            ownerRestaurantIdString = ownerRestaurantId.toString();
        }

        @Test
        @DisplayName("Should return true when security context restaurantId matches parameter")
        void should_ReturnTrue_When_IdsMatch() {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(ownerRestaurantId));

            boolean isOwner = restaurantService.isOwner(ownerRestaurantIdString);

            assertTrue(isOwner, "Should return true when IDs match");
            verify(securityService).getCurrentUserRestaurantId();
        }

        @Test
        @DisplayName("Should return false when security context restaurantId does not match parameter")
        void should_ReturnFalse_When_IdsDoNotMatch() {
            UUID otherRestaurantId = UUID.randomUUID();

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(otherRestaurantId));

            boolean isOwner = restaurantService.isOwner(ownerRestaurantIdString);

            assertFalse(isOwner, "Should return false when IDs do not match");
            verify(securityService).getCurrentUserRestaurantId();
        }

        @Test
        @DisplayName("Should return false when security context has no restaurantId")
        void should_ReturnFalse_When_SecurityContextIsEmpty() {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.empty());

            boolean isOwner = restaurantService.isOwner(ownerRestaurantIdString);

            assertFalse(isOwner, "Should return false when context is empty");
            verify(securityService).getCurrentUserRestaurantId();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when provided ID string is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdStringIsInvalid() {
            String invalidUuidString = "this-is-not-a-uuid";

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(ownerRestaurantId));

            assertThrows(IllegalArgumentException.class, () -> {
                restaurantService.isOwner(invalidUuidString);
            });

            verify(securityService).getCurrentUserRestaurantId();
        }
    }
}