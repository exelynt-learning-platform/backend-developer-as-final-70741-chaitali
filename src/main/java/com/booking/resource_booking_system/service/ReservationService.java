package com.booking.resource_booking_system.service;
import com.booking.resource_booking_system.exception.ResourceNotFoundException;
import com.booking.resource_booking_system.dto.ReservationRequest;
import com.booking.resource_booking_system.dto.ReservationUpdateRequest;
import com.booking.resource_booking_system.entity.Reservation;
import com.booking.resource_booking_system.entity.Resource;
import com.booking.resource_booking_system.entity.Status;
import com.booking.resource_booking_system.entity.User;
import com.booking.resource_booking_system.repository.ReservationRepository;
import com.booking.resource_booking_system.repository.ResourceRepository;
import com.booking.resource_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public Reservation createReservation(
            ReservationRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reservation not found"));

        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().isEqual(request.getStartTime())) {
            throw new RuntimeException(
                    "End time must be after start time");
        }
        if (request.getStartTime().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException(
                    "Start time cannot be in the past");
        }
        boolean alreadyBooked =
                reservationRepository
                        .existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                                resource.getId(),
                                request.getEndTime(),
                                request.getStartTime()
                        );

        if (alreadyBooked) {
            throw new RuntimeException(
                    "Resource is already booked for the selected time");
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
                        new ResourceNotFoundException("Reservation not found"));
    }

    public Reservation updateReservation(
            Long id,
            ReservationUpdateRequest request) {

        Reservation reservation = getReservationById(id);

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
}