package com.xenplan.app.domain.entity;

import com.xenplan.app.domain.enum.EventCategory;
import com.xenplan.app.domain.enum.EventStatus;
import jakarta.persistence.*;
        import lombok.*;
        import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_events_category", columnList = "category"),
                @Index(name = "idx_events_status", columnList = "status"),
                @Index(name = "idx_events_city", columnList = "city"),
                @Index(name = "idx_events_start_date", columnList = "start_date")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private EventCategory category;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(length = 100, nullable = false)
    private String venue;

    @Column(length = 50, nullable = false)
    private String city;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private Double unitPrice;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
