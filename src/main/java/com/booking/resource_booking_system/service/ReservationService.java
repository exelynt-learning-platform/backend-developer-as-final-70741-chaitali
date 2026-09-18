package com.booking.resource_booking_system.service;

import com.booking.resource_booking_system.dto.ReservationRequest;
import com.booking.resource_booking_system.dto.ReservationUpdateRequest;
import com.booking.resource_booking_system.entity.Reservation;
import com.booking.resource_booking_system.entity.Resource;
import com.booking.resource_booking_system.entity.Status;
import com.booking.resource_booking_system.entity.User;
import com.booking.resource_booking_system.exception.ConflictException;
import com.booking.resource_booking_system.exception.ResourceNotFoundException;
import com.booking.resource_booking_system.repository.ReservationRepository;
import com.booking.resource_booking_system.repository.ResourceRepository;
import com.booking.resource_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Set<Status> ACTIVE_STATUSES =
            Set.of(Status.PENDING, Status.CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reservation createReservation(
            ReservationRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Resource resource = resourceRepository
                .findByIdForUpdate(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resource not found"));

        validateReservationTimes(
                request.getStartTime(),
                request.getEndTime(),
                true
        );

        if (!resource.isAvailable()) {
            throw new ConflictException(
                    "Resource is currently unavailable");
        }

        boolean overlapping =
                reservationRepository.existsOverlappingReservation(
                        resource.getId(),
                        request.getStartTime(),
                        request.getEndTime(),
                        List.copyOf(ACTIVE_STATUSES)
                );

        if (overlapping) {
            throw new ConflictException(
                    "Resource is already booked for the selected time");
        }

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(Status.PENDING);

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Page<Reservation> getReservations(
            String username,
            boolean isAdmin,
            Status status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "minPrice must be less than or equal to maxPrice");
        }

        Specification<Reservation> specification =
                (root, query, cb) -> cb.conjunction();

        if (!isAdmin) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("user").get("username"),
                                    username
                            )
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("price"),
                                    minPrice
                            )
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
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

    @Transactional(readOnly = true)
    public Reservation getReservationById(
            Long id,
            String username,
            boolean isAdmin) {

        Reservation reservation =
                getReservationOrThrow(id);

        if (!isAdmin
                && !reservation.getUser()
                .getUsername()
                .equals(username)) {

            throw new AccessDeniedException(
                    "Access denied");
        }

        return reservation;
    }

    @Transactional
    public Reservation updateReservation(
            Long id,
            ReservationUpdateRequest request) {

        Reservation reservation =
                getReservationOrThrow(id);

        Resource resource =
                resourceRepository.findByIdForUpdate(
                                reservation.getResource().getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found"
                                ));

        validateReservationTimes(
                request.getStartTime(),
                request.getEndTime(),
                true
        );

        if (request.getStatus() != Status.CANCELLED
                && !resource.isAvailable()) {

            throw new ConflictException(
                    "Resource is currently unavailable");
        }

        if (ACTIVE_STATUSES.contains(
                request.getStatus())) {

            boolean overlapping =
                    reservationRepository
                            .existsOverlappingReservationExcludingId(
                                    resource.getId(),
                                    reservation.getId(),
                                    request.getStartTime(),
                                    request.getEndTime(),
                                    List.copyOf(ACTIVE_STATUSES)
                            );

            if (overlapping) {
                throw new ConflictException(
                        "Resource is already booked for the selected time"
                );
            }
        }

        reservation.setStartTime(
                request.getStartTime());

        reservation.setEndTime(
                request.getEndTime());

        reservation.setPrice(
                request.getPrice());

        reservation.setStatus(
                request.getStatus());

        return reservationRepository.save(
                reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {

        Reservation reservation =
                getReservationOrThrow(id);

        reservationRepository.delete(
                reservation);
    }

    private Reservation getReservationOrThrow(
            Long id) {

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found"
                        ));
    }

    private void validateReservationTimes(
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean checkPastTime) {

        if (startTime == null
                || endTime == null) {

            throw new IllegalArgumentException(
                    "Start time and end time are required");
        }

        if (!endTime.isAfter(startTime)) {

            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        if (checkPastTime
                && startTime.isBefore(
                LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Start time cannot be in the past");
        }
    }
}