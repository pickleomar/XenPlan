package com.xenplan.app.service;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.domain.exception.ForbiddenException;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.repository.ReservationRepository;
import com.xenplan.app.service.impl.EventServiceImpl;
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
@DisplayName("EventService Business Logic Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private User adminUser;
    private User organizerUser;
    private User clientUser;
    private Event draftEvent;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .password("password123")
                .role(Role.ADMIN)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();

        organizerUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Organizer")
                .lastName("User")
                .email("organizer@example.com")
                .password("password123")
                .role(Role.ORGANIZER)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();

        clientUser = User.builder()
                .id(UUID.randomUUID())
                .firstName("Client")
                .lastName("User")
                .email("client@example.com")
                .password("password123")
                .role(Role.CLIENT)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();

        draftEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .description("Test Description")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(7).plusHours(3))
                .venue("Test Venue")
                .city("Test City")
                .maxCapacity(100)
                .unitPrice(new BigDecimal("50.00"))
                .status(EventStatus.DRAFT)
                .organizer(organizerUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create event as ADMIN")
    void testCreateEventAsAdmin() {
        // Given
        Event newEvent = Event.builder()
                .title("New Event")
                .description("Description")
                .category(EventCategory.CONFERENCE)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(5))
                .venue("Venue")
                .city("City")
                .maxCapacity(50)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        Event created = eventService.createEvent(newEvent, adminUser);

        // Then
        assertNotNull(created);
        assertEquals(EventStatus.DRAFT, created.getStatus());
        assertEquals(adminUser, created.getOrganizer());
        assertNotNull(created.getCreatedAt());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should create event as ORGANIZER")
    void testCreateEventAsOrganizer() {
        // Given
        Event newEvent = Event.builder()
                .title("New Event")
                .category(EventCategory.SPORT)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .venue("Venue")
                .city("City")
                .maxCapacity(50)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Event created = eventService.createEvent(newEvent, organizerUser);

        // Then
        assertNotNull(created);
        assertEquals(organizerUser, created.getOrganizer());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw ForbiddenException when CLIENT tries to create event")
    void testCreateEventAsClient() {
        // Given
        Event newEvent = Event.builder()
                .title("New Event")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .venue("Venue")
                .city("City")
                .maxCapacity(50)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        // When/Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            eventService.createEvent(newEvent, clientUser);
        });

        assertEquals("Only ADMIN or ORGANIZER can create events", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when start date is in past")
    void testCreateEventWithPastStartDate() {
        // Given
        Event newEvent = Event.builder()
                .title("New Event")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().minusDays(1)) // Past date
                .endDate(LocalDateTime.now().plusDays(1))
                .venue("Venue")
                .city("City")
                .maxCapacity(50)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            eventService.createEvent(newEvent, adminUser);
        });

        assertEquals("Event start date must be in the future", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ConflictException when end date is before start date")
    void testCreateEventWithInvalidEndDate() {
        // Given
        Event newEvent = Event.builder()
                .title("New Event")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(5)) // Before start date
                .venue("Venue")
                .city("City")
                .maxCapacity(50)
                .unitPrice(new BigDecimal("30.00"))
                .build();

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            eventService.createEvent(newEvent, adminUser);
        });

        assertEquals("Event end date must be after start date", exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should publish DRAFT event")
    void testPublishEvent() {
        // Given
        when(eventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventService.publishEvent(draftEvent.getId(), organizerUser);

        // Then
        assertEquals(EventStatus.PUBLISHED, draftEvent.getStatus());
        verify(eventRepository).save(draftEvent);
    }

    @Test
    @DisplayName("Should throw ConflictException when publishing non-DRAFT event")
    void testPublishNonDraftEvent() {
        // Given
        Event publishedEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Published Event")
                .category(EventCategory.CONCERT)
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(7).plusHours(3))
                .venue("Venue")
                .city("City")
                .maxCapacity(100)
                .unitPrice(new BigDecimal("50.00"))
                .status(EventStatus.PUBLISHED)
                .organizer(organizerUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(eventRepository.findById(publishedEvent.getId())).thenReturn(Optional.of(publishedEvent));

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            eventService.publishEvent(publishedEvent.getId(), organizerUser);
        });

        assertEquals("Only DRAFT events can be published", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ForbiddenException when non-creator tries to publish")
    void testPublishEventByNonCreator() {
        // Given
        User otherOrganizer = User.builder()
                .id(UUID.randomUUID())
                .email("other@example.com")
                .role(Role.ORGANIZER)
                .build();

        when(eventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));

        // When/Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            eventService.publishEvent(draftEvent.getId(), otherOrganizer);
        });

        assertEquals("Only event creator or ADMIN can publish events", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete event with no reservations")
    void testDeleteEventWithNoReservations() {
        // Given
        when(eventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));
        when(reservationRepository.countByEventId(draftEvent.getId())).thenReturn(0L);

        // When
        eventService.deleteEvent(draftEvent.getId(), organizerUser);

        // Then
        verify(eventRepository).delete(draftEvent);
    }

    @Test
    @DisplayName("Should throw ConflictException when deleting event with reservations")
    void testDeleteEventWithReservations() {
        // Given
        when(eventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));
        when(reservationRepository.countByEventId(draftEvent.getId())).thenReturn(5L);

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            eventService.deleteEvent(draftEvent.getId(), organizerUser);
        });

        assertTrue(exception.getMessage().contains("Cannot delete event with"));
        assertTrue(exception.getMessage().contains("existing reservation"));
        verify(eventRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should calculate available seats correctly")
    void testCalculateAvailableSeats() {
        // Given
        when(eventRepository.findById(draftEvent.getId())).thenReturn(Optional.of(draftEvent));
        when(reservationRepository.sumNumberOfSeatsByEventIdAndStatusNot(any(), any())).thenReturn(25);

        // When
        Integer available = eventService.calculateAvailableSeats(draftEvent.getId());

        // Then
        assertEquals(75, available); // 100 capacity - 25 reserved
    }

    @Test
    @DisplayName("Should throw NotFoundException when event not found")
    void testUpdateEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            eventService.updateEvent(nonExistentId, draftEvent, organizerUser);
        });

        assertEquals("Event not found", exception.getMessage());
    }
}

