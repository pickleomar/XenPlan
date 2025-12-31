package com.xenplan.app.service;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventService {
    
    /**
     * Create event (ADMIN or ORGANIZER only)
     */
    Event createEvent(Event event, User creator);
    
    /**
     * Update event (only creator or ADMIN)
     */
    Event updateEvent(UUID eventId, Event eventData, User user);
    
    /**
     * Publish event (DRAFT → PUBLISHED)
     */
    void publishEvent(UUID eventId, User user);
    
    /**
     * Cancel event (handle existing reservations)
     */
    void cancelEvent(UUID eventId, User user);
    
    /**
     * Delete event (only if no reservations exist)
     */
    void deleteEvent(UUID eventId, User user);
    
    /**
     * Calculate available seats for an event
     */
    Integer calculateAvailableSeats(UUID eventId);
    
    /**
     * Auto-detect and mark FINISHED events (endDate < now)
     */
    void markFinishedEvents();
    
    /**
     * Get event by ID
     */
    Optional<Event> findById(UUID eventId);
    
    /**
     * Get event by ID with organizer eagerly loaded (prevents lazy loading issues)
     */
    Optional<Event> findByIdWithOrganizer(UUID eventId);
    
    /**
     * Get all published events
     */
    List<Event> findAllPublished();
    
    /**
     * Get events by category
     */
    List<Event> findByCategory(EventCategory category);
    
    /**
     * Get events by city
     */
    List<Event> findByCity(String city);
    
    /**
     * Get events by organizer
     */
    List<Event> findByOrganizer(User organizer);
    
    /**
     * Get events by organizer ID (more reliable with detached entities)
     */
    List<Event> findByOrganizerId(UUID organizerId);
}

