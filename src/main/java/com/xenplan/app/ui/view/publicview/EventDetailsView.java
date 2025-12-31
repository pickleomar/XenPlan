package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public EventDetailsView(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        
        setSizeFull();
        setPadding(false); // Remove default padding to control layout better
        setSpacing(false);
        setAlignItems(Alignment.CENTER); // Center the content container
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
            this.event = eventService.findByIdWithOrganizer(this.eventId)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
            
            setupView();
        } catch (Exception e) {
            event.rerouteToError(NotFoundException.class);
        }
    }

    private void setupView() {
        removeAll();

        // 1. MAIN CONTAINER (Centers content and limits width)
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setMaxWidth("1100px");
        contentContainer.setWidthFull();
        contentContainer.setPadding(true);
        contentContainer.setSpacing(true);

        // --- Back Button ---
        Button backButton = new Button("Back to Events", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate(""));
        contentContainer.add(backButton);

        // --- Image Banner ---
        if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
            Image eventImage = new Image(event.getImageUrl(), "Event image");
            eventImage.setWidthFull();
            eventImage.setMaxHeight("400px");
            eventImage.getStyle().set("object-fit", "cover");
            eventImage.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
            eventImage.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");
            contentContainer.add(eventImage);
        }

        // --- Split Layout (Info Left | Booking Card Right) ---
        HorizontalLayout splitLayout = new HorizontalLayout();
        splitLayout.setWidthFull();
        splitLayout.setSpacing(true);
        // Make it wrap on mobile
        splitLayout.getStyle().set("flex-wrap", "wrap"); 
        splitLayout.setAlignItems(Alignment.START);

        // 2. LEFT COLUMN: Description & Main Info
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.getStyle().set("flex", "1 1 600px"); // Grow, Shrink, Base width 600px

        // Title
        H1 title = new H1(event.getTitle());
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "2.5rem");

        // Badges Row
        HorizontalLayout badges = new HorizontalLayout();
        badges.add(createBadge(event.getCategory().name(), "var(--lumo-primary-color)"));
        
        /*if (event.getStatus() == EventStatus.CANCELLED) {
            badges.add(createBadge("CANCELLED", "var(--lumo-error-color)"));
        } else if (event.getStatus() == EventStatus.PUBLISHED) {
            badges.add(createBadge("PUBLISHED", "var(--lumo-success-color)"));
        }*/
        
        // Organizer info
        HorizontalLayout organizerRow = new HorizontalLayout();
        organizerRow.setAlignItems(Alignment.CENTER);
        Icon userIcon = new Icon(VaadinIcon.USER_CARD);
        userIcon.setSize("1.2rem");
        userIcon.setColor("var(--lumo-secondary-text-color)");
        
        String orgName = "Unknown Organizer";
        if(event.getOrganizer() != null) {
            orgName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
        }
        Span organizerText = new Span("Organized by " + orgName);
        organizerText.getStyle().set("color", "var(--lumo-secondary-text-color)");
        organizerRow.add(userIcon, organizerText);

        // Description Body
        Paragraph description = new Paragraph(event.getDescription());
        description.getStyle().set("font-size", "1.1rem");
        description.getStyle().set("line-height", "1.6");
        description.getStyle().set("color", "var(--lumo-body-text-color)");

        leftColumn.add(title, badges, organizerRow, description);


        // 3. RIGHT COLUMN: The "Booking Card"
        VerticalLayout bookingCard = new VerticalLayout();
        bookingCard.setSpacing(true);
        bookingCard.setPadding(true);
        bookingCard.setWidth("350px"); // Fixed width for the sidebar
        // Make card stand out
        bookingCard.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        bookingCard.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        bookingCard.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
        // On mobile, let it fill width
        bookingCard.getStyle().set("min-width", "300px");
        bookingCard.getStyle().set("flex", "1 1 300px");

        H3 cardTitle = new H3("Event Details");
        cardTitle.getStyle().set("margin-top", "0");

        // Details List inside Card
        bookingCard.add(cardTitle);
        bookingCard.add(createDetailRow(VaadinIcon.CALENDAR, "Start", event.getStartDate().format(DATE_FORMATTER)));
        bookingCard.add(createDetailRow(VaadinIcon.CLOCK, "End", event.getEndDate().format(DATE_FORMATTER)));
        bookingCard.add(createDetailRow(VaadinIcon.MAP_MARKER, "Location", event.getVenue() + ", " + event.getCity()));
        
        Integer availableSeats = eventService.calculateAvailableSeats(event.getId());
        bookingCard.add(createDetailRow(VaadinIcon.GROUP, "Availability", availableSeats + " / " + event.getMaxCapacity() + " seats"));

        // Price Tag
        H1 priceTag = new H1("$" + event.getUnitPrice());
        priceTag.getStyle().set("color", "var(--lumo-primary-color)");
        priceTag.getStyle().set("margin", "1rem 0 0.5rem 0");
        priceTag.getStyle().set("align-self", "center");
        bookingCard.add(priceTag);

        // Action Button
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null && event.getStatus() == EventStatus.PUBLISHED && availableSeats > 0) {
            Button reserveButton = new Button("Book Now", new Icon(VaadinIcon.TICKET));
            reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            reserveButton.setWidthFull();
            reserveButton.addClickListener(e -> openReservationDialog(currentUser));
            bookingCard.add(reserveButton);
        } else if (currentUser == null) {
            Button loginButton = new Button("Login to Book");
            loginButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
            loginButton.setWidthFull();
            loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));
            bookingCard.add(loginButton);
        } else {
            Button soldOutBtn = new Button("Sold Out");
            soldOutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            soldOutBtn.setWidthFull();
            soldOutBtn.setEnabled(false);
            bookingCard.add(soldOutBtn);
        }

        // Combine
        splitLayout.add(leftColumn, bookingCard);
        contentContainer.add(splitLayout);
        add(contentContainer);
    }

    // --- Helper Methods for Styling ---

    private Span createBadge(String text, String colorVar) {
        Span badge = new Span(text);
        badge.getStyle().set("background-color", colorVar + "1A"); // 10% opacity hex
        badge.getStyle().set("color", colorVar);
        badge.getStyle().set("padding", "0.25rem 0.75rem");
        badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        badge.getStyle().set("font-weight", "600");
        badge.getStyle().set("font-size", "0.8rem");
        badge.getStyle().set("text-transform", "uppercase");
        return badge;
    }

    private HorizontalLayout createDetailRow(VaadinIcon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        
        Icon i = new Icon(icon);
        i.setSize("1.2rem");
        i.setColor("var(--lumo-primary-color)");
        i.getStyle().set("margin-right", "10px");

        VerticalLayout textStack = new VerticalLayout();
        textStack.setPadding(false);
        textStack.setSpacing(false);
        
        Span lbl = new Span(label);
        lbl.getStyle().set("font-size", "0.75rem");
        lbl.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        Span val = new Span(value);
        val.getStyle().set("font-size", "0.95rem");
        val.getStyle().set("font-weight", "500");
        
        textStack.add(lbl, val);
        row.add(i, textStack);
        return row;
    }

    private void openReservationDialog(User user) {
        ReservationDialog dialog = new ReservationDialog(event, user, eventService, reservationService);
        dialog.setOnReservationComplete(success -> {
            if (success) {
                setupView(); // Refresh to update seat count
            }
        });
        dialog.open();
    }
}