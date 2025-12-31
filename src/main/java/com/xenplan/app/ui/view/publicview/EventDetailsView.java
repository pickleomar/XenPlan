package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
            // Use findByIdWithOrganizer to eagerly load organizer and avoid lazy loading issues
            this.event = eventService.findByIdWithOrganizer(this.eventId)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
            
            setupView();
        } catch (IllegalArgumentException e) {
            event.rerouteToError(NotFoundException.class);
        } catch (NotFoundException e) {
            event.rerouteToError(NotFoundException.class);
        } catch (Exception e) {
            event.rerouteToError(NotFoundException.class);
        }
    }

    private void setupView() {
        removeAll();
        
        // Main content layout
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSpacing(true);
        mainContent.setPadding(false);
        mainContent.setWidthFull();
        
        // Image section (if available)
        if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
            Image eventImage = new Image(event.getImageUrl(), "Event image");
            eventImage.setWidthFull();
            eventImage.setMaxHeight("400px");
            eventImage.getStyle().set("object-fit", "cover");
            eventImage.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
            eventImage.getStyle().set("margin-bottom", "2rem");
            mainContent.add(eventImage);
        }
        
        // Title and badges section
        VerticalLayout headerSection = new VerticalLayout();
        headerSection.setSpacing(true);
        headerSection.setPadding(false);
        headerSection.setWidthFull();
        
        H2 title = new H2(event.getTitle());
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "0.5rem");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        title.getStyle().set("font-weight", "600");
        
        HorizontalLayout badgesLayout = new HorizontalLayout();
        badgesLayout.setSpacing(true);
        badgesLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Category badge
        Div categoryBadge = new Div();
        categoryBadge.setText(event.getCategory().name());
        categoryBadge.getStyle().set("display", "inline-block");
        categoryBadge.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        categoryBadge.getStyle().set("color", "var(--lumo-primary-color)");
        categoryBadge.getStyle().set("padding", "0.5rem 1rem");
        categoryBadge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        categoryBadge.getStyle().set("font-weight", "600");
        categoryBadge.getStyle().set("font-size", "var(--lumo-font-size-s)");
        categoryBadge.getStyle().set("text-transform", "uppercase");
        categoryBadge.getStyle().set("letter-spacing", "0.05em");
        
        badgesLayout.add(categoryBadge);
        
        // Status badge
        if (event.getStatus() == EventStatus.CANCELLED) {
            Div statusBadge = new Div();
            statusBadge.setText("CANCELLED");
            statusBadge.getStyle().set("display", "inline-block");
            statusBadge.getStyle().set("background", "var(--lumo-error-color-10pct)");
            statusBadge.getStyle().set("color", "var(--lumo-error-color)");
            statusBadge.getStyle().set("padding", "0.5rem 1rem");
            statusBadge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            statusBadge.getStyle().set("font-weight", "600");
            statusBadge.getStyle().set("font-size", "var(--lumo-font-size-s)");
            statusBadge.getStyle().set("text-transform", "uppercase");
            statusBadge.getStyle().set("letter-spacing", "0.05em");
            badgesLayout.add(statusBadge);
        } else if (event.getStatus() == EventStatus.PUBLISHED) {
            Div statusBadge = new Div();
            statusBadge.setText("PUBLISHED");
            statusBadge.getStyle().set("display", "inline-block");
            statusBadge.getStyle().set("background", "var(--lumo-success-color-10pct)");
            statusBadge.getStyle().set("color", "var(--lumo-success-color)");
            statusBadge.getStyle().set("padding", "0.5rem 1rem");
            statusBadge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            statusBadge.getStyle().set("font-weight", "600");
            statusBadge.getStyle().set("font-size", "var(--lumo-font-size-s)");
            statusBadge.getStyle().set("text-transform", "uppercase");
            statusBadge.getStyle().set("letter-spacing", "0.05em");
            badgesLayout.add(statusBadge);
        }
        
        headerSection.add(title, badgesLayout);
        mainContent.add(headerSection);
        
        // Description
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(event.getDescription());
            description.getStyle().set("font-size", "var(--lumo-font-size-l)");
            description.getStyle().set("line-height", "1.8");
            description.getStyle().set("color", "var(--lumo-body-text-color)");
            description.getStyle().set("margin-top", "1rem");
            description.getStyle().set("margin-bottom", "1rem");
            mainContent.add(description);
        }
        
        // Event details section with modern card design
        VerticalLayout detailsSection = new VerticalLayout();
        detailsSection.setSpacing(true);
        detailsSection.setPadding(true);
        detailsSection.getStyle().set("background", "var(--lumo-contrast-5pct)");
        detailsSection.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        detailsSection.getStyle().set("margin-top", "2rem");
        detailsSection.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        
        H3 detailsTitle = new H3("Event Details");
        detailsTitle.getStyle().set("margin-top", "0");
        detailsTitle.getStyle().set("margin-bottom", "1rem");
        detailsTitle.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        detailsTitle.getStyle().set("font-weight", "600");
        detailsSection.add(detailsTitle);
        
        // Date and time with icon
        HorizontalLayout dateLayout = createDetailRowWithIcon(
                VaadinIcon.CALENDAR, 
                "Date & Time", 
                event.getStartDate().format(DATE_FORMATTER) + " - " + 
                event.getEndDate().format(DATE_FORMATTER));
        detailsSection.add(dateLayout);
        
        // Location with icon
        HorizontalLayout locationLayout = createDetailRowWithIcon(
                VaadinIcon.MAP_MARKER, 
                "Location", 
                event.getVenue() + ", " + event.getCity());
        detailsSection.add(locationLayout);
        
        // Capacity and availability
        Integer availableSeats = eventService.calculateAvailableSeats(event.getId());
        HorizontalLayout capacityLayout = createDetailRowWithIcon(
                VaadinIcon.TICKET, 
                "Capacity", 
                availableSeats + " seats available out of " + event.getMaxCapacity());
        detailsSection.add(capacityLayout);
        
        // Price with icon
        HorizontalLayout priceLayout = createDetailRowWithIcon(
                VaadinIcon.DOLLAR, 
                "Price", 
                formatPrice(event.getUnitPrice()) + " per seat");
        detailsSection.add(priceLayout);
        
        // Organizer with icon (safely access organizer - should be eagerly loaded)
        if (event.getOrganizer() != null) {
            try {
                String organizerName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
                HorizontalLayout organizerLayout = createDetailRowWithIcon(
                        VaadinIcon.USER, 
                        "Organizer", 
                        organizerName);
                detailsSection.add(organizerLayout);
            } catch (Exception e) {
                // If organizer is still lazy-loaded, show email or skip
                if (event.getOrganizer().getEmail() != null) {
                    HorizontalLayout organizerLayout = createDetailRowWithIcon(
                            VaadinIcon.USER, 
                            "Organizer", 
                            event.getOrganizer().getEmail());
                    detailsSection.add(organizerLayout);
                }
            }
        }
        
        mainContent.add(detailsSection);
        
        // Reservation button section
        VerticalLayout actionSection = new VerticalLayout();
        actionSection.setSpacing(true);
        actionSection.setPadding(true);
        actionSection.setAlignItems(FlexComponent.Alignment.CENTER);
        actionSection.getStyle().set("margin-top", "2rem");
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null && event.getStatus() == EventStatus.PUBLISHED && availableSeats > 0) {
            Button reserveButton = new Button("Reserve Seats", new Icon(VaadinIcon.TICKET));
            reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            reserveButton.getStyle().set("font-size", "var(--lumo-font-size-l)");
            reserveButton.getStyle().set("padding", "1rem 2rem");
            reserveButton.addClickListener(e -> openReservationDialog(currentUser));
            actionSection.add(reserveButton);
        } else if (currentUser == null) {
            Paragraph loginPrompt = new Paragraph("Please login to make a reservation");
            loginPrompt.getStyle().set("color", "var(--lumo-secondary-text-color)");
            loginPrompt.getStyle().set("font-style", "italic");
            loginPrompt.getStyle().set("font-size", "var(--lumo-font-size-m)");
            actionSection.add(loginPrompt);
        } else if (availableSeats == 0) {
            Div soldOutBadge = new Div();
            soldOutBadge.setText("SOLD OUT");
            soldOutBadge.getStyle().set("background", "var(--lumo-error-color-10pct)");
            soldOutBadge.getStyle().set("color", "var(--lumo-error-color)");
            soldOutBadge.getStyle().set("padding", "1rem 2rem");
            soldOutBadge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            soldOutBadge.getStyle().set("font-weight", "600");
            soldOutBadge.getStyle().set("font-size", "var(--lumo-font-size-l)");
            soldOutBadge.getStyle().set("text-transform", "uppercase");
            soldOutBadge.getStyle().set("letter-spacing", "0.1em");
            actionSection.add(soldOutBadge);
        }
        
        mainContent.add(actionSection);
        add(mainContent);
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
    
    private HorizontalLayout createDetailRowWithIcon(VaadinIcon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("padding", "1rem 0");
        row.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-20pct)");
        
        Icon iconComponent = new Icon(icon);
        iconComponent.setSize("1.5rem");
        iconComponent.getStyle().set("color", "var(--lumo-primary-color)");
        
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(false);
        contentLayout.setPadding(false);
        contentLayout.setFlexGrow(1);
        
        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.getStyle().set("font-weight", "600");
        labelDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
        labelDiv.getStyle().set("font-size", "var(--lumo-font-size-s)");
        labelDiv.getStyle().set("text-transform", "uppercase");
        labelDiv.getStyle().set("letter-spacing", "0.05em");
        
        Div valueDiv = new Div();
        valueDiv.setText(value);
        valueDiv.getStyle().set("font-size", "var(--lumo-font-size-m)");
        valueDiv.getStyle().set("color", "var(--lumo-body-text-color)");
        valueDiv.getStyle().set("margin-top", "0.25rem");
        
        contentLayout.add(labelDiv, valueDiv);
        row.add(iconComponent, contentLayout);
        
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

