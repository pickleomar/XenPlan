package com.xenplan.app.service;

import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {
    
    /**
     * Create a new reservation with strict business rules:
     * - Event must be PUBLISHED
     * - Event must not be FINISHED
     * - Seats must be between 1-10
     * - Seats must not exceed available capacity
     * - Auto-generates reservation code (EVT-XXXXX format)
     * - Auto-calculates totalAmount = seats × unitPrice
     */
    Reservation createReservation(UUID eventId, Integer numberOfSeats, String comment, User user);
    
    /**
     * Cancel a reservation only if >48 hours before event start
     */
    void cancelReservation(UUID reservationId, User user);
    
    /**
     * Confirm a pending reservation
     */
    void confirmReservation(UUID reservationId, User user);
    
    /**
     * Get all reservations for a user
     */
    List<Reservation> getReservationsByUser(User user);

    /**
     * Get all reservations for a user with eager loading of related entities
     */
    List<Reservation> getUserReservations(User user);
    
    /**
     * Verify reservation by code
     */
    Optional<Reservation> verifyReservationByCode(String reservationCode);
    
    /**
     * Calculate total seats reserved for an event (excluding CANCELLED)
     */
    Integer calculateTotalReservedSeats(UUID eventId);
    
    /**
     * Get reservation by ID
     */
    Optional<Reservation> findById(UUID reservationId);
}