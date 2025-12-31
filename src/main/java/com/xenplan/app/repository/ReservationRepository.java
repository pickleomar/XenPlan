package com.xenplan.app.repository;

import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    /**
     * Find reservations by user ID, ordered by reservation date descending
     */
    List<Reservation> findByUserIdOrderByReservationDateDesc(UUID userId);

    /**
     * Find reservations by user with eager fetching of Event and Organizer to prevent LazyInitializationException
     */
    @Query("SELECT r FROM Reservation r JOIN FETCH r.event e LEFT JOIN FETCH e.organizer WHERE r.user = :user ORDER BY r.reservationDate DESC")
    List<Reservation> findByUserWithDetails(@Param("user") User user);
    
    /**
     * Find reservation by unique reservation code
     */
    Optional<Reservation> findByReservationCode(String reservationCode);
    
    /**
     * Calculate total reserved seats for an event, excluding cancelled reservations
     */
    @Query("SELECT COALESCE(SUM(r.numberOfSeats), 0) FROM Reservation r " +
           "WHERE r.event.id = :eventId AND r.status != :status")
    Integer sumNumberOfSeatsByEventIdAndStatusNot(
            @Param("eventId") UUID eventId, 
            @Param("status") ReservationStatus status
    );
    
    /**
     * Count reservations for an event
     */
    long countByEventId(UUID eventId);
}