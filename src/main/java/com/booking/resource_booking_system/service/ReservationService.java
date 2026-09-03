package com.booking.resource_booking_system.service;

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
                        new RuntimeException("Resource not found"));

        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().isEqual(request.getStartTime())) {
            throw new RuntimeException(
                    "End time must be after start time");
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

    public Page<Reservation> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    public Page<Reservation> getUserReservations(
            String username,
            Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return reservationRepository.findAll(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("user").get("id"),
                                user.getId()
                        ),
                pageable
        );
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Reservation not found"));
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