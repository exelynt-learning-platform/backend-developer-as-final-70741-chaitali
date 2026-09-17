package com.booking.resource_booking_system.service;

import com.booking.resource_booking_system.dto.ReservationRequest;
import com.booking.resource_booking_system.entity.Reservation;
import com.booking.resource_booking_system.entity.Resource;
import com.booking.resource_booking_system.entity.Status;
import com.booking.resource_booking_system.entity.User;
import com.booking.resource_booking_system.exception.ConflictException;
import com.booking.resource_booking_system.repository.ReservationRepository;
import com.booking.resource_booking_system.repository.ResourceRepository;
import com.booking.resource_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Resource resource;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .description("Meeting room")
                .available(true)
                .build();
    }

    @Test
    void shouldCreateReservationSuccessfully() {

        LocalDateTime startTime =
                LocalDateTime.now().plusHours(2);

        LocalDateTime endTime =
                startTime.plusHours(1);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPrice(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.existsOverlappingReservation(
                eq(1L),
                eq(startTime),
                eq(endTime),
                anyList()
        )).thenReturn(false);

        Reservation savedReservation =
                Reservation.builder()
                        .id(1L)
                        .user(user)
                        .resource(resource)
                        .startTime(startTime)
                        .endTime(endTime)
                        .price(new BigDecimal("500.00"))
                        .status(Status.PENDING)
                        .build();

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        Reservation result =
                reservationService.createReservation(
                        request,
                        "testuser"
                );

        assertNotNull(result);
        assertEquals(Status.PENDING, result.getStatus());
        assertEquals(
                new BigDecimal("500.00"),
                result.getPrice()
        );
        assertEquals(user, result.getUser());
        assertEquals(resource, result.getResource());

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenStartTimeIsInPast() {

        LocalDateTime startTime =
                LocalDateTime.now().minusHours(1);

        LocalDateTime endTime =
                LocalDateTime.now().plusHours(1);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPrice(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(resource));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.createReservation(
                                request,
                                "testuser"
                        )
                );

        assertEquals(
                "Start time cannot be in the past",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenEndTimeIsBeforeStartTime() {

        LocalDateTime startTime =
                LocalDateTime.now().plusHours(3);

        LocalDateTime endTime =
                startTime.minusHours(1);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPrice(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(resource));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reservationService.createReservation(
                                request,
                                "testuser"
                        )
                );

        assertEquals(
                "End time must be after start time",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenResourceIsUnavailable() {

        resource.setAvailable(false);

        LocalDateTime startTime =
                LocalDateTime.now().plusHours(2);

        LocalDateTime endTime =
                startTime.plusHours(1);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPrice(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(resource));

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> reservationService.createReservation(
                                request,
                                "testuser"
                        )
                );

        assertEquals(
                "Resource is currently unavailable",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectOverlappingReservation() {

        LocalDateTime startTime =
                LocalDateTime.now().plusHours(2);

        LocalDateTime endTime =
                startTime.plusHours(1);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPrice(new BigDecimal("500.00"));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.existsOverlappingReservation(
                eq(1L),
                eq(startTime),
                eq(endTime),
                anyList()
        )).thenReturn(true);

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () -> reservationService.createReservation(
                                request,
                                "testuser"
                        )
                );

        assertEquals(
                "Resource is already booked for the selected time",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectWhenMinPriceIsGreaterThanMaxPrice() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.getReservations(
                        "testuser",
                        true,
                        null,
                        new BigDecimal("1000.00"),
                        new BigDecimal("500.00"),
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                10
                        )
                )
        );

        verify(reservationRepository, never())
                .findAll(
                        any(org.springframework.data.jpa.domain.Specification.class),
                        any(org.springframework.data.domain.Pageable.class)
                );
    }
}