package com.xenplan.app.service;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.repository.ReservationRepository;
import com.xenplan.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.xenplan.app.service.impl.EventServiceImpl;
import com.xenplan.app.service.impl.ReservationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test using real database (H2 in-memory)
 * Tests the full flow with actual JPA operations
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false"
})
@Import({ReservationServiceImpl.class, EventServiceImpl.class})
@DisplayName("ReservationService Integration Tests")
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private User organizer;
    private User client;
    private Event publishedEvent;

    @BeforeEach
    void setUp() {
        // Clean database
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create organizer
        organizer = User.builder()
                .firstName("Organizer")
                .lastName("User")
                .email("organizer@test.com")
                .password("$2a$12$test")
                .role(Role.ORGANIZER)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();
        organizer = userRepository.save(organizer);

        // Create client
        client = User.builder()
                .firstName("Client")
                .lastName("User")
                .email("client@test.com")
                .password("$2a$12$test")
                .role(Role.CLIENT)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();
        client = userRepository.save(client);

        // Create and publish event
        Event event = Event.builder()
                .title("Integration Test Concert")
                .description("Test description")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(7).plusHours(3))
                .venue("Test Venue")
                .city("Test City")
                .maxCapacity(100)
                .unitPrice(new BigDecimal("50.00"))
                .status(EventStatus.DRAFT)
                .organizer(organizer)
                .createdAt(LocalDateTime.now())
                .build();
        publishedEvent = eventRepository.save(event);
        
        // Publish the event
        eventService.publishEvent(publishedEvent.getId(), organizer);
        publishedEvent = eventRepository.findById(publishedEvent.getId()).orElseThrow();
    }

    @Test
    @DisplayName("Integration: Create and retrieve reservation")
    void testCreateAndRetrieveReservation() {
        // When
        Reservation reservation = reservationService.createReservation(
                publishedEvent.getId(), 3, "Test comment", client
        );

        // Then
        assertNotNull(reservation.getId());
        assertEquals(3, reservation.getNumberOfSeats());
        assertEquals(new BigDecimal("150.00"), reservation.getTotalAmount());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertTrue(reservation.getReservationCode().startsWith("EVT-"));
        assertEquals("Test comment", reservation.getComment());

        // Verify in database
        Reservation saved = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertEquals(client.getId(), saved.getUser().getId());
        assertEquals(publishedEvent.getId(), saved.getEvent().getId());
    }

    @Test
    @DisplayName("Integration: Multiple reservations affect capacity")
    void testMultipleReservationsAffectCapacity() {
        // When - create multiple reservations
        reservationService.createReservation(publishedEvent.getId(), 10, null, client);
        reservationService.createReservation(publishedEvent.getId(), 20, null, client);
        reservationService.createReservation(publishedEvent.getId(), 30, null, client);

        // Then - check available seats
        Integer available = eventService.calculateAvailableSeats(publishedEvent.getId());
        assertEquals(40, available); // 100 - 60 = 40

        // Try to reserve more than available
        assertThrows(Exception.class, () -> {
            reservationService.createReservation(publishedEvent.getId(), 50, null, client);
        });
    }

    @Test
    @DisplayName("Integration: Get reservations by user")
    void testGetReservationsByUser() {
        // Given
        reservationService.createReservation(publishedEvent.getId(), 2, "First", client);
        reservationService.createReservation(publishedEvent.getId(), 3, "Second", client);

        // When
        List<Reservation> reservations = reservationService.getReservationsByUser(client);

        // Then
        assertEquals(2, reservations.size());
        assertEquals("Second", reservations.get(0).getComment()); // Should be ordered by date desc
    }

    @Test
    @DisplayName("Integration: Cancel reservation updates status")
    void testCancelReservation() {
        // Given
        Reservation reservation = reservationService.createReservation(
                publishedEvent.getId(), 5, null, client
        );

        // When
        reservationService.cancelReservation(reservation.getId(), client);

        // Then
        Reservation cancelled = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus());

        // Cancelled reservations should free up capacity
        Integer available = eventService.calculateAvailableSeats(publishedEvent.getId());
        assertEquals(100, available); // All seats available again
    }

    @Test
    @DisplayName("Integration: Verify reservation by code")
    void testVerifyReservationByCode() {
        // Given
        Reservation reservation = reservationService.createReservation(
                publishedEvent.getId(), 2, null, client
        );

        // When
        var found = reservationService.verifyReservationByCode(reservation.getReservationCode());

        // Then
        assertTrue(found.isPresent());
        assertEquals(reservation.getId(), found.get().getId());
    }
}

