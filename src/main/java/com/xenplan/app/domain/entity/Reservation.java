package com.xenplan.app.domain.entity;

import com.xenplan.app.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(
    name = "reservations",
    indexes = {
        @Index(name = "idx_reservations_code", columnList = "reservation_code"),
        @Index(name = "idx_reservations_status", columnList = "status")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36, nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Event is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Number of seats must be at least 1")
    @Max(value = 10, message = "Number of seats must not exceed 10")
    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount must be greater than or equal to 0")
    @Digits(integer = 8, fraction = 2, message = "Total amount must have at most 8 integer digits and 2 decimal places")
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @NotNull(message = "Reservation date is required")
    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReservationStatus status;

    @NotBlank(message = "Reservation code is required")
    @Pattern(regexp = "EVT-[A-Z0-9]{5}", message = "Reservation code must match format EVT-XXXXX")
    @Column(name = "reservation_code", length = 12, nullable = false, unique = true)
    private String reservationCode;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    @Column(length = 500)
    private String comment;

    @PrePersist
    void onCreate() {
        this.reservationDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReservationStatus.PENDING;
        }
    }
}
