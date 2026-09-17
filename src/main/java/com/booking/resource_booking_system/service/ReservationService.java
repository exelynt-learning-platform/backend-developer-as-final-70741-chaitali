package com.booking.resource_booking_system.service;
import com.booking.resource_booking_system.exception.ConflictException;
import com.booking.resource_booking_system.dto.ReservationRequest;
import com.booking.resource_booking_system.dto.ReservationUpdateRequest;
import com.booking.resource_booking_system.entity.Reservation;
import com.booking.resource_booking_system.entity.Resource;
import com.booking.resource_booking_system.entity.Status;
import com.booking.resource_booking_system.entity.User;
import com.booking.resource_booking_system.exception.ResourceNotFoundException;
import com.booking.resource_booking_system.repository.ReservationRepository;
import com.booking.resource_booking_system.repository.ResourceRepository;
import com.booking.resource_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    private static final List<Status> ACTIVE_STATUSES =
            List.of(Status.PENDING, Status.CONFIRMED);

    @Transactional
    public Reservation createReservation(
            ReservationRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resource resource = resourceRepository.findByIdForUpdate(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resource not found"));

        validateReservationTimes(
                request.getStartTime(),
                request.getEndTime()
        );

        if (!resource.isAvailable()) {
           throw new ConflictException(
        "Resource is currently unavailable"
           );
        }

        boolean alreadyBooked =
                reservationRepository.existsOverlappingReservation(
                        resource.getId(),
                        request.getStartTime(),
                        request.getEndTime(),
                        ACTIVE_STATUSES
                );

        if (alreadyBooked) {
           throw new ConflictException(
           "Resource is already booked for the selected time"
           );
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .status(Status.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    public Page<Reservation> getReservations(
            String username,
            boolean isAdmin,
            Status status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "minPrice cannot be greater than maxPrice"
            );
        }

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (!isAdmin) {

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("user").get("id"),
                                    user.getId()
                            )
            );
        }

        if (status != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (minPrice != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("price"),
                                    minPrice
                            )
            );
        }

        if (maxPrice != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("price"),
                                    maxPrice
                            )
            );
        }

        return reservationRepository.findAll(
                specification,
                pageable
        );
    }

    public Reservation getReservationById(Long id) {

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found"
                        ));
    }

    @Transactional
    public Reservation updateReservation(
            Long id,
            ReservationUpdateRequest request) {

        Reservation reservation = getReservationById(id);

        validateReservationTimes(
                request.getStartTime(),
                request.getEndTime()
        );

        Resource resource = resourceRepository.findByIdForUpdate(
                reservation.getResource().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Resource not found"));

        if (!resource.isAvailable()) {
            throw new ConflictException(
                    "Resource is currently unavailable"
            );
        }

        if (request.getStatus() == Status.PENDING
                || request.getStatus() == Status.CONFIRMED) {

            boolean alreadyBooked =
                    reservationRepository
                            .existsOverlappingReservationExcludingId(
                                    resource.getId(),
                                    reservation.getId(),
                                    request.getStartTime(),
                                    request.getEndTime(),
                                    ACTIVE_STATUSES
                            );

            if (alreadyBooked) {
                throw new ConflictException(
                        "Resource is already booked for the selected time"
                );

            }
        }

        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(request.getStatus());

        return reservationRepository.save(reservation);
    }

    public void deleteReservation(Long id) {

        Reservation reservation = getReservationById(id);

        reservationRepository.delete(reservation);
    }

    private void validateReservationTimes(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Start time cannot be in the past"
            );
        }
    }
}