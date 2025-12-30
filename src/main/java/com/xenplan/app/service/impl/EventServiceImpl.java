package com.xenplan.app.service.impl;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.domain.exception.ForbiddenException;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.repository.ReservationRepository;
import com.xenplan.app.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public EventServiceImpl(EventRepository eventRepository, ReservationRepository reservationRepository) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Event createEvent(Event event, User creator) {
        // Business rule: Only ADMIN or ORGANIZER can create events
        String role = creator.getRole().name();
        if (!role.equals("ADMIN") && !role.equals("ORGANIZER")) {
            throw new ForbiddenException("Only ADMIN or ORGANIZER can create events");
        }

        // Business rule: Start date must be in the future
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Event start date must be in the future");
        }

        // Business rule: End date must be after start date
        if (event.getEndDate().isBefore(event.getStartDate()) || 
            event.getEndDate().isEqual(event.getStartDate())) {
            throw new ConflictException("Event end date must be after start date");
        }

        // Set defaults
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizer(creator);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(UUID eventId, Event eventData, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // Business rule: Only creator or ADMIN can update
        boolean isCreator = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("Only event creator or ADMIN can update events");
        }

        // Business rule: Cannot update PUBLISHED or FINISHED events
        if (event.getStatus() == EventStatus.PUBLISHED || event.getStatus() == EventStatus.FINISHED) {
            throw new ConflictException("Cannot update PUBLISHED or FINISHED events");
        }

        // Update fields
        if (eventData.getTitle() != null) {
            event.setTitle(eventData.getTitle());
        }
        if (eventData.getDescription() != null) {
            event.setDescription(eventData.getDescription());
        }
        if (eventData.getCategory() != null) {
            event.setCategory(eventData.getCategory());
        }
        if (eventData.getStartDate() != null) {
            // Validate start date is in future
            if (eventData.getStartDate().isBefore(LocalDateTime.now())) {
                throw new ConflictException("Event start date must be in the future");
            }
            event.setStartDate(eventData.getStartDate());
        }
        if (eventData.getEndDate() != null) {
            // Validate end date is after start date
            LocalDateTime startDate = eventData.getStartDate() != null ? 
                    eventData.getStartDate() : event.getStartDate();
            if (eventData.getEndDate().isBefore(startDate) || 
                eventData.getEndDate().isEqual(startDate)) {
                throw new ConflictException("Event end date must be after start date");
            }
            event.setEndDate(eventData.getEndDate());
        }
        if (eventData.getVenue() != null) {
            event.setVenue(eventData.getVenue());
        }
        if (eventData.getCity() != null) {
            event.setCity(eventData.getCity());
        }
        if (eventData.getMaxCapacity() != null) {
            event.setMaxCapacity(eventData.getMaxCapacity());
        }
        if (eventData.getUnitPrice() != null) {
            event.setUnitPrice(eventData.getUnitPrice());
        }
        if (eventData.getImageUrl() != null) {
            event.setImageUrl(eventData.getImageUrl());
        }

        event.setUpdatedAt(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Override
    public void publishEvent(UUID eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // Business rule: Only creator or ADMIN can publish
        boolean isCreator = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("Only event creator or ADMIN can publish events");
        }

        // Business rule: Can only publish DRAFT events
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ConflictException("Only DRAFT events can be published");
        }

        // Business rule: Start date must be in the future
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot publish event with start date in the past");
        }

        event.setStatus(EventStatus.PUBLISHED);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    }

    @Override
    public void cancelEvent(UUID eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // Business rule: Only creator or ADMIN can cancel
        boolean isCreator = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("Only event creator or ADMIN can cancel events");
        }

        // Business rule: Cannot cancel already cancelled or finished events
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("Event is already cancelled");
        }
        if (event.getStatus() == EventStatus.FINISHED) {
            throw new ConflictException("Cannot cancel a FINISHED event");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);

        // Note: Existing reservations remain, but new reservations cannot be created
        // (handled by ReservationService business rules)
    }

    @Override
    public void deleteEvent(UUID eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // Business rule: Only creator or ADMIN can delete
        boolean isCreator = event.getOrganizer().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("Only event creator or ADMIN can delete events");
        }

        // Business rule: Can only delete if no reservations exist
        long reservationCount = reservationRepository.countByEventId(eventId);
        if (reservationCount > 0) {
            throw new ConflictException(
                    String.format("Cannot delete event with %d existing reservation(s)", reservationCount)
            );
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateAvailableSeats(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Integer totalReserved = reservationRepository.sumNumberOfSeatsByEventIdAndStatusNot(
                eventId, ReservationStatus.CANCELLED
        );
        if (totalReserved == null) {
            totalReserved = 0;
        }

        return Math.max(0, event.getMaxCapacity() - totalReserved);
    }

    @Override
    public void markFinishedEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> activeEvents = eventRepository.findByStatusInAndEndDateBefore(
                List.of(EventStatus.DRAFT, EventStatus.PUBLISHED), now
        );

        for (Event event : activeEvents) {
            event.setStatus(EventStatus.FINISHED);
            event.setUpdatedAt(now);
        }

        if (!activeEvents.isEmpty()) {
            eventRepository.saveAll(activeEvents);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(UUID eventId) {
        return eventRepository.findById(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAllPublished() {
        return eventRepository.findByStatusOrderByStartDateAsc(EventStatus.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByCategory(EventCategory category) {
        return eventRepository.findByCategoryAndStatusOrderByStartDateAsc(category, EventStatus.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByCity(String city) {
        return eventRepository.findByCityAndStatusOrderByStartDateAsc(city, EventStatus.PUBLISHED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByOrganizer(User organizer) {
        return eventRepository.findByOrganizerOrderByCreatedAtDesc(organizer);
    }
}

