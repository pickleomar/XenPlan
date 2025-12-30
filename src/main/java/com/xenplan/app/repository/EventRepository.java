package com.xenplan.app.repository;

import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    
    /**
     * Find all published events, ordered by start date ascending
     */
    List<Event> findByStatusOrderByStartDateAsc(EventStatus status);
    
    /**
     * Find events by category and status, ordered by start date ascending
     */
    List<Event> findByCategoryAndStatusOrderByStartDateAsc(EventCategory category, EventStatus status);
    
    /**
     * Find events by city and status, ordered by start date ascending
     */
    List<Event> findByCityAndStatusOrderByStartDateAsc(String city, EventStatus status);
    
    /**
     * Find events by organizer, ordered by creation date descending
     */
    List<Event> findByOrganizerOrderByCreatedAtDesc(User organizer);
    
    /**
     * Find events by status and end date before (for auto-marking FINISHED)
     */
    List<Event> findByStatusInAndEndDateBefore(List<EventStatus> statuses, LocalDateTime endDate);
}
