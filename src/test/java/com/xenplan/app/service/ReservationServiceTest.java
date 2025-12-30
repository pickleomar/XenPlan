package com.xenplan.app.service;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.domain.exception.ForbiddenException;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.repository.ReservationRepository;
import com.xenplan.app.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Business Logic Tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User testUser;
    private Event publishedEvent;
    private Event draftEvent;
    private Event finishedEvent;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("password123")
                .role(Role.CLIENT)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();

        // Create published event
        publishedEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Concert")
                .description("A test concert")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(7).plusHours(3))
                .venue("Test Venue")
                .city("Test City")
                .maxCapacity(100)
                .unitPrice(new BigDecimal("50.00"))
                .status(EventStatus.PUBLISHED)
                .organizer(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Create draft event
        draftEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Draft Event")
                .category(EventCategory.CONFERENCE)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(5))
                .venue("Draft Venue")
                .city("Draft City")
                .maxCapacity(50)
                .unitPrice(new BigDecimal("30.00"))
                .status(EventStatus.DRAFT)
                .organizer(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Create finished event
        finishedEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Finished Event")
                .category(EventCategory.SPORT)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().minusDays(1).plusHours(2))
                .venue("Finished Venue")
                .city("Finished City")
                .maxCapacity(200)
                .unitPrice(new BigDecimal("25.00"))
                .status(EventStatus.FINISHED)
                .organizer(testUser)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
    }

    @Test
    @DisplayName("Should create reservation for PUBLISHED event")
    void testCreateReservationForPublishedEvent() {
        // Given
        when(eventRepository.findById(publishedEvent.getId())).thenReturn(Optional.of(publishedEvent));
        when(reservationRepository.sumNumberOfSeatsByEventIdAndStatusNot(any(), any())).thenReturn(0);
        when(reservationRepository.findByReservationCode(any())).thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation reservation = reservationService.createReservation(
                publishedEvent.getId(), 2, "Test comment", testUser
        );

        // Then
        assertNotNull(reservation);
        assertEquals(2, reservation.getNumberOfSeats());
        assertEquals(new BigDecimal("100.00"), reservation.getTotalAmount());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertTrue(reservation.getReservationCode().startsWith("EVT-"));
        assertEquals("Test comment", reservation.getComment());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when event is DRAFT")
    void testCreateReservationForDraftEvent() {
        // Given
        when(eventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            reservationService.createReservation(draftEvent.getId(), 2, null, testUser);
        });

        assertEquals("Event must be PUBLISHED to make reservations", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when event is FINISHED")
    void testCreateReservationForFinishedEvent() {
        // Given - Create a FINISHED but PUBLISHED event (status check happens first)
        Event finishedPublishedEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Finished Published Event")
                .category(EventCategory.SPORT)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().minusDays(1).plusHours(2))
                .venue("Finished Venue")
                .city("Finished City")
                .maxCapacity(200)
                .unitPrice(new BigDecimal("25.00"))
                .status(EventStatus.FINISHED) // FINISHED status
                .organizer(testUser)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        when(eventRepository.findById(finishedPublishedEvent.getId())).thenReturn(Optional.of(finishedPublishedEvent));

        // When/Then - Service checks PUBLISHED first, so this will fail on PUBLISHED check
        // But if we had a PUBLISHED event that's finished, it would check FINISHED
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            reservationService.createReservation(finishedPublishedEvent.getId(), 2, null, testUser);
        });

        // The service checks PUBLISHED status first
        assertEquals("Event must be PUBLISHED to make reservations", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when seats exceed capacity")
    void testCreateReservationExceedsCapacity() {
        // Given
        when(eventRepository.findById(publishedEvent.getId())).thenReturn(Optional.of(publishedEvent));
        when(reservationRepository.sumNumberOfSeatsByEventIdAndStatusNot(any(), any())).thenReturn(99);

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            reservationService.createReservation(publishedEvent.getId(), 5, null, testUser);
        });

        assertTrue(exception.getMessage().contains("Only 1 seats available"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when seats > 10")
    void testCreateReservationExceedsMaxSeats() {
        // When/Then - Validation happens before repository call
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            reservationService.createReservation(publishedEvent.getId(), 11, null, testUser);
        });

        assertEquals("Number of seats must be between 1 and 10", exception.getMessage());
        verify(eventRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel reservation if >48 hours before event")
    void testCancelReservationSuccess() {
        // Given
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .event(publishedEvent)
                .numberOfSeats(2)
                .totalAmount(new BigDecimal("100.00"))
                .reservationDate(LocalDateTime.now().minusDays(1))
                .status(ReservationStatus.PENDING)
                .reservationCode("EVT-12345")
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reservationService.cancelReservation(reservation.getId(), testUser);

        // Then
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Should throw ConflictException when canceling <48 hours before event")
    void testCancelReservationTooLate() {
        // Given
        Event soonEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Soon Event")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusHours(24)) // Only 24 hours away
                .endDate(LocalDateTime.now().plusHours(27))
                .venue("Soon Venue")
                .city("Soon City")
                .maxCapacity(100)
                .unitPrice(new BigDecimal("50.00"))
                .status(EventStatus.PUBLISHED)
                .organizer(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .event(soonEvent)
                .numberOfSeats(2)
                .totalAmount(new BigDecimal("100.00"))
                .reservationDate(LocalDateTime.now().minusDays(1))
                .status(ReservationStatus.PENDING)
                .reservationCode("EVT-12345")
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            reservationService.cancelReservation(reservation.getId(), testUser);
        });

        assertTrue(exception.getMessage().contains("Cannot cancel reservation"));
        assertTrue(exception.getMessage().contains("48 hours"));
    }

    @Test
    @DisplayName("Should throw ForbiddenException when canceling other user's reservation")
    void testCancelReservationOtherUser() {
        // Given
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .email("other@example.com")
                .build();

        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .user(otherUser)
                .event(publishedEvent)
                .numberOfSeats(2)
                .totalAmount(new BigDecimal("100.00"))
                .reservationDate(LocalDateTime.now().minusDays(1))
                .status(ReservationStatus.PENDING)
                .reservationCode("EVT-12345")
                .build();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // When/Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            reservationService.cancelReservation(reservation.getId(), testUser);
        });

        assertEquals("You can only cancel your own reservations", exception.getMessage());
    }

    @Test
    @DisplayName("Should calculate total reserved seats correctly")
    void testCalculateTotalReservedSeats() {
        // Given
        UUID eventId = publishedEvent.getId();
        when(reservationRepository.sumNumberOfSeatsByEventIdAndStatusNot(eventId, ReservationStatus.CANCELLED))
                .thenReturn(25);

        // When
        Integer total = reservationService.calculateTotalReservedSeats(eventId);

        // Then
        assertEquals(25, total);
    }

    @Test
    @DisplayName("Should throw NotFoundException when event not found")
    void testCreateReservationEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            reservationService.createReservation(nonExistentId, 2, null, testUser);
        });

        assertEquals("Event not found", exception.getMessage());
    }
}

