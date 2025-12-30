package com.xenplan.app.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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

        // Title
        H3 title = new H3(event.getTitle());
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "var(--lumo-font-size-xl)");

        // Category badge
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

        // Description (truncated)
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(
                    event.getDescription().length() > 150 
                            ? event.getDescription().substring(0, 150) + "..." 
                            : event.getDescription()
            );
            description.getStyle().set("color", "var(--lumo-secondary-text-color)");
            description.getStyle().set("margin", "0.5rem 0");
            add(description);
        }

        // Event details
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        // Date and time
        Div dateInfo = new Div();
        dateInfo.setText("📅 " + event.getStartDate().format(DATE_FORMATTER));
        dateInfo.getStyle().set("font-size", "var(--lumo-font-size-s)");
        dateInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        dateInfo.getStyle().set("margin-bottom", "0.25rem");

        // Venue and city
        Div locationInfo = new Div();
        locationInfo.setText("📍 " + event.getVenue() + ", " + event.getCity());
        locationInfo.getStyle().set("font-size", "var(--lumo-font-size-s)");
        locationInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        locationInfo.getStyle().set("margin-bottom", "0.25rem");

        // Price
        Div priceInfo = new Div();
        priceInfo.setText("💰 " + formatPrice(event.getUnitPrice()) + " per seat");
        priceInfo.getStyle().set("font-size", "var(--lumo-font-size-s)");
        priceInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        priceInfo.getStyle().set("margin-bottom", "0.25rem");

        // Available seats
        if (availableSeats != null) {
            Div seatsInfo = new Div();
            seatsInfo.setText("🎫 " + availableSeats + " seats available");
            seatsInfo.getStyle().set("font-size", "var(--lumo-font-size-s)");
            seatsInfo.getStyle().set("color", availableSeats > 0 ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
            seatsInfo.getStyle().set("font-weight", "500");
            details.add(seatsInfo);
        }

        details.add(dateInfo, locationInfo, priceInfo);

        // Action button
        Button viewButton = new Button("View Details");
        viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewButton.setWidthFull();
        
        RouterLink detailsLink = new RouterLink("", EventDetailsView.class, 
                new com.vaadin.flow.router.RouteParameters(
                        java.util.Map.of("eventId", event.getId().toString())));
        detailsLink.add(viewButton);
        detailsLink.getStyle().set("text-decoration", "none");

        // Status indicator
        if (event.getStatus() == EventStatus.CANCELLED) {
            Span statusBadge = new Span("CANCELLED");
            statusBadge.getStyle().set("background", "var(--lumo-error-color-10pct)");
            statusBadge.getStyle().set("color", "var(--lumo-error-color)");
            statusBadge.getStyle().set("padding", "0.25rem 0.75rem");
            statusBadge.getStyle().set("border-radius", "var(--lumo-border-radius-s)");
            statusBadge.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            statusBadge.getStyle().set("font-weight", "500");
            viewButton.setEnabled(false);
            viewButton.setText("Event Cancelled");
            add(statusBadge);
        }

        add(titleLayout, details, detailsLink);
    }

    private String formatPrice(BigDecimal price) {
        return String.format("$%.2f", price);
    }
}
