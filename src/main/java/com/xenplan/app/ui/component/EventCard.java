package com.xenplan.app.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.ui.view.publicview.EventDetailsView;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class EventCard extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public EventCard(Event event, Integer availableSeats) {
        setPadding(true);
        setSpacing(true);
        setWidth("100%");
        getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("background", "var(--lumo-base-color)");
        getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // --- 1. Header (Title & Category) ---
        H3 title = new H3(event.getTitle());
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "var(--lumo-font-size-xl)");

        Span categoryBadge = new Span(event.getCategory().name());
        categoryBadge.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        categoryBadge.getStyle().set("color", "var(--lumo-primary-color)");
        categoryBadge.getStyle().set("padding", "0.25rem 0.75rem");
        categoryBadge.getStyle().set("border-radius", "var(--lumo-border-radius-s)");
        categoryBadge.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        categoryBadge.getStyle().set("font-weight", "500");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidthFull();
        titleLayout.setAlignItems(Alignment.CENTER);
        titleLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        titleLayout.add(title, categoryBadge);
        
        // FIX: Add title layout immediately so it appears at the top
        add(titleLayout);

        // --- 2. Description ---
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(
                    event.getDescription().length() > 150 
                            ? event.getDescription().substring(0, 150) + "..." 
                            : event.getDescription()
            );
            description.getStyle().set("color", "var(--lumo-secondary-text-color)");
            description.getStyle().set("margin", "0.5rem 0");
            add(description); // Added AFTER title
        }

        // --- 3. Event Details with Icons ---
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        // Date Row
        details.add(createDetailRow(VaadinIcon.CALENDAR, event.getStartDate().format(DATE_FORMATTER)));

        // Location Row
        details.add(createDetailRow(VaadinIcon.MAP_MARKER, event.getVenue() + ", " + event.getCity()));

        // Price Row
        details.add(createDetailRow(VaadinIcon.DOLLAR, formatPrice(event.getUnitPrice()) + " per seat"));

        // Seats Row
        if (availableSeats != null) {
            HorizontalLayout seatsRow = createDetailRow(VaadinIcon.TICKET, availableSeats + " seats available");
            // Highlight color for seats text
            Span textSpan = (Span) seatsRow.getComponentAt(1);
            textSpan.getStyle().set("color", availableSeats > 0 ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
            textSpan.getStyle().set("font-weight", "500");
            details.add(seatsRow);
        }

        add(details);

        // --- 4. Actions ---
        Button viewButton = new Button("View Details");
        viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewButton.setWidthFull();
        
        RouterLink detailsLink = new RouterLink("", EventDetailsView.class, 
                new com.vaadin.flow.router.RouteParameters(
                        java.util.Map.of("eventId", event.getId().toString())));
        detailsLink.add(viewButton);
        detailsLink.getStyle().set("text-decoration", "none");

        add(detailsLink);

        // Cancelled Status Overlay
        if (event.getStatus() == EventStatus.CANCELLED) {
            viewButton.setEnabled(false);
            viewButton.setText("Event Cancelled");
            viewButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        }
    }

    // Helper to create "Icon + Text" rows nicely
    private HorizontalLayout createDetailRow(VaadinIcon icon, String text) {
        Icon i = new Icon(icon);
        i.setSize("var(--lumo-font-size-s)"); // Small professional icon size
        i.getStyle().set("color", "var(--lumo-tertiary-text-color)"); // Subtle icon color
        
        Span s = new Span(text);
        s.getStyle().set("font-size", "var(--lumo-font-size-s)");
        s.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        HorizontalLayout row = new HorizontalLayout(i, s);
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "0.5rem"); // Better spacing control
        row.getStyle().set("margin-bottom", "0.25rem");
        return row;
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%.2f", price);
    }
}