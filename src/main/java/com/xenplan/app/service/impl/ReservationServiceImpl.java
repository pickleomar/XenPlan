package com.xenplan.app.service.impl;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.domain.exception.ForbiddenException;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.repository.ReservationRepository;
import com.xenplan.app.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, EventRepository eventRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public Reservation createReservation(UUID eventId, Integer numberOfSeats, String comment, User user) {
        // Validate seats
        if (numberOfSeats == null || numberOfSeats < 1 || numberOfSeats > 10) {
            throw new ConflictException("Number of seats must be between 1 and 10");
        }

        // Find event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // Business rule: Event must be PUBLISHED
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ConflictException("Event must be PUBLISHED to make reservations");
        }

        // Business rule: Event must not be FINISHED
        if (event.getStatus() == EventStatus.FINISHED) {
            throw new ConflictException("Cannot reserve seats for a FINISHED event");
        }

        // Business rule: Check available capacity
        Integer totalReserved = calculateTotalReservedSeats(eventId);
        Integer availableSeats = Math.max(0, event.getMaxCapacity() - totalReserved);
        
        if (numberOfSeats > availableSeats) {
            throw new ConflictException(
                    String.format("Only %d seats available, requested %d", availableSeats, numberOfSeats)
            );
        }

        // Auto-calculate total amount
        java.math.BigDecimal totalAmount = event.getUnitPrice()
                .multiply(java.math.BigDecimal.valueOf(numberOfSeats));

        // Auto-generate reservation code (EVT-XXXXX format)
        String reservationCode = generateReservationCode();

        // Create reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .numberOfSeats(numberOfSeats)
                .totalAmount(totalAmount)
                .reservationDate(LocalDateTime.now())
                .status(ReservationStatus.PENDING)
                .reservationCode(reservationCode)
                .comment(comment)
                .build();

        return reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(UUID reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        // Business rule: Only reservation owner can cancel
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only cancel your own reservations");
        }

        // Business rule: Cannot cancel already cancelled reservation
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ConflictException("Reservation is already cancelled");
        }

        // Business rule: Can only cancel if >48 hours before event start
        LocalDateTime eventStart = reservation.getEvent().getStartDate();
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilEvent = java.time.Duration.between(now, eventStart).toHours();

        if (hoursUntilEvent <= 48) {
            throw new ConflictException(
                    String.format("Cannot cancel reservation. Event starts in %d hours. Minimum 48 hours required.", hoursUntilEvent)
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    public void confirmReservation(UUID reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        // Business rule: Only event organizer or admin can confirm
        Event event = reservation.getEvent();
        boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isOrganizer && !isAdmin) {
            throw new ForbiddenException("Only event organizer or admin can confirm reservations");
        }

        // Business rule: Can only confirm PENDING reservations
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ConflictException("Only PENDING reservations can be confirmed");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(User user) {
        // Uses the new repository method with fetch join
        return reservationRepository.findByUserWithDetails(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> verifyReservationByCode(String reservationCode) {
        return reservationRepository.findByReservationCode(reservationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalReservedSeats(UUID eventId) {
        Integer result = reservationRepository.sumNumberOfSeatsByEventIdAndStatusNot(
                eventId, ReservationStatus.CANCELLED
        );
        return result != null ? result : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(UUID reservationId) {
        return reservationRepository.findById(reservationId);
    }

    /**
     * Generate unique reservation code in format EVT-XXXXX
     * Uses uppercase alphanumeric characters
     */
    private String generateReservationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;
        int maxAttempts = 100;
        int attempts = 0;

        do {
            StringBuilder sb = new StringBuilder("EVT-");
            for (int i = 0; i < 5; i++) {
                int index = ThreadLocalRandom.current().nextInt(chars.length());
                sb.append(chars.charAt(index));
            }
            code = sb.toString();
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new ConflictException("Failed to generate unique reservation code");
            }
        } while (reservationRepository.findByReservationCode(code).isPresent());

        return code;
    }
}