package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.service.EventService;
import com.xenplan.app.service.ReservationService;
import com.xenplan.app.ui.component.ReservationDialog;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

@Route(value = "events/:eventId", layout = MainLayout.class)
@PageTitle("Event Details | XenPlan")
@AnonymousAllowed
public class EventDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private Event event;
    private UUID eventId;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT);

    public EventDetailsView(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String eventIdParam = event.getRouteParameters().get("eventId").orElse(null);
        
        if (eventIdParam == null) {
            event.rerouteToError(NotFoundException.class);
            return;
        }
        
        try {
            this.eventId = UUID.fromString(eventIdParam);
            this.event = eventService.findById(this.eventId)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
            
            setupView();
        } catch (IllegalArgumentException e) {
            event.rerouteToError(NotFoundException.class);
        } catch (NotFoundException e) {
            event.rerouteToError(NotFoundException.class);
        }
    }

    private void setupView() {
        removeAll();
        
        // Title and category
        H2 title = new H2(event.getTitle());
        title.getStyle().set("margin-top", "0");
        
        Div categoryBadge = new Div();
        categoryBadge.setText(event.getCategory().name());
        categoryBadge.getStyle().set("display", "inline-block");
        categoryBadge.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        categoryBadge.getStyle().set("color", "var(--lumo-primary-color)");
        categoryBadge.getStyle().set("padding", "0.5rem 1rem");
        categoryBadge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        categoryBadge.getStyle().set("font-weight", "500");
        categoryBadge.getStyle().set("margin-bottom", "1rem");
        
        // Status badge
        if (event.getStatus() == EventStatus.CANCELLED) {
            Div statusBadge = new Div();
            statusBadge.setText("CANCELLED");
            statusBadge.getStyle().set("display", "inline-block");
            statusBadge.getStyle().set("background", "var(--lumo-error-color-10pct)");
            statusBadge.getStyle().set("color", "var(--lumo-error-color)");
            statusBadge.getStyle().set("padding", "0.5rem 1rem");
            statusBadge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            statusBadge.getStyle().set("font-weight", "500");
            statusBadge.getStyle().set("margin-left", "0.5rem");
            add(statusBadge);
        }
        
        add(title, categoryBadge);
        
        // Description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(event.getDescription());
            description.getStyle().set("font-size", "var(--lumo-font-size-m)");
            description.getStyle().set("line-height", "1.6");
            description.getStyle().set("color", "var(--lumo-body-text-color)");
            add(description);
        }
        
        // Event details grid
        VerticalLayout detailsSection = new VerticalLayout();
        detailsSection.setSpacing(true);
        detailsSection.setPadding(true);
        detailsSection.getStyle().set("background", "var(--lumo-contrast-5pct)");
        detailsSection.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        detailsSection.getStyle().set("margin-top", "1rem");
        
        H3 detailsTitle = new H3("Event Details");
        detailsTitle.getStyle().set("margin-top", "0");
        detailsSection.add(detailsTitle);
        
        // Date and time
        Div dateInfo = createDetailRow("📅 Date & Time", 
                event.getStartDate().format(DATE_FORMATTER) + " - " + 
                event.getEndDate().format(DATE_FORMATTER));
        detailsSection.add(dateInfo);
        
        // Location
        Div locationInfo = createDetailRow("📍 Location", 
                event.getVenue() + ", " + event.getCity());
        detailsSection.add(locationInfo);
        
        // Capacity and availability
        Integer availableSeats = eventService.calculateAvailableSeats(event.getId());
        Div capacityInfo = createDetailRow("🎫 Capacity", 
                availableSeats + " seats available out of " + event.getMaxCapacity());
        detailsSection.add(capacityInfo);
        
        // Price
        Div priceInfo = createDetailRow("💰 Price", 
                formatPrice(event.getUnitPrice()) + " per seat");
        detailsSection.add(priceInfo);
        
        // Organizer
        if (event.getOrganizer() != null) {
            Div organizerInfo = createDetailRow("👤 Organizer", 
                    event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName());
            detailsSection.add(organizerInfo);
        }
        
        add(detailsSection);
        
        // Reservation button (only for authenticated users and published events)
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null && event.getStatus() == EventStatus.PUBLISHED && availableSeats > 0) {
            Button reserveButton = new Button("Reserve Seats");
            reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            reserveButton.addClickListener(e -> openReservationDialog(currentUser));
            reserveButton.getStyle().set("margin-top", "2rem");
            add(reserveButton);
        } else if (currentUser == null) {
            Paragraph loginPrompt = new Paragraph("Please login to make a reservation");
            loginPrompt.getStyle().set("color", "var(--lumo-secondary-text-color)");
            loginPrompt.getStyle().set("font-style", "italic");
            loginPrompt.getStyle().set("margin-top", "2rem");
            add(loginPrompt);
        } else if (availableSeats == 0) {
            Paragraph soldOut = new Paragraph("This event is sold out");
            soldOut.getStyle().set("color", "var(--lumo-error-color)");
            soldOut.getStyle().set("font-weight", "500");
            soldOut.getStyle().set("margin-top", "2rem");
            add(soldOut);
        }
    }

    private Div createDetailRow(String label, String value) {
        Div row = new Div();
        row.getStyle().set("display", "flex");
        row.getStyle().set("justify-content", "space-between");
        row.getStyle().set("padding", "0.75rem 0");
        row.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
        
        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.getStyle().set("font-weight", "500");
        labelDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        Div valueDiv = new Div();
        valueDiv.setText(value);
        valueDiv.getStyle().set("text-align", "right");
        
        row.add(labelDiv, valueDiv);
        return row;
    }

    private void openReservationDialog(User user) {
        ReservationDialog dialog = new ReservationDialog(event, user, eventService, reservationService);
        dialog.setOnReservationComplete(success -> {
            if (success) {
                // Refresh the view
                setupView();
            }
        });
        dialog.open();
    }

    private String formatPrice(BigDecimal price) {
        return String.format("$%.2f", price);
    }
}

