package com.xenplan.app.ui.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.component.ConfirmDialog;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

@Route(value = "admin/events", layout = MainLayout.class)
@PageTitle("Event Management | XenPlan")
@RolesAllowed("ADMIN")
public class EventManagementView extends VerticalLayout {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final User currentUser;
    private Grid<Event> eventsGrid;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public EventManagementView(EventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        
        setupHeader();
        setupGrid();
        loadEvents();
    }

    private void setupHeader() {
        H2 title = new H2("Event Management");
        title.getStyle().set("margin-top", "0");
        
        RouterLink backLink = new RouterLink("← Back to Dashboard", AdminDashboardView.class);
        backLink.getStyle().set("text-decoration", "none");
        backLink.getStyle().set("margin-bottom", "1rem");
        
        add(title, backLink);
    }

    private void setupGrid() {
        eventsGrid = new Grid<>(Event.class, false);
        eventsGrid.setWidthFull();
        eventsGrid.setAllRowsVisible(true);
        
        eventsGrid.addColumn(Event::getTitle)
                .setHeader("Title")
                .setSortable(true)
                .setAutoWidth(true);
        
        eventsGrid.addColumn(e -> {
            // Safely access organizer - should be eagerly loaded now
            User organizer = e.getOrganizer();
            if (organizer != null) {
                return organizer.getFirstName() + " " + organizer.getLastName();
            }
            return "Unknown";
        })
        .setHeader("Organizer")
        .setSortable(true)
        .setAutoWidth(true);
        
        eventsGrid.addColumn(e -> e.getCategory().name())
                .setHeader("Category")
                .setSortable(true)
                .setAutoWidth(true);
        
        eventsGrid.addColumn(e -> e.getStartDate().format(DATE_FORMATTER))
                .setHeader("Start Date")
                .setSortable(true)
                .setAutoWidth(true);
        
        eventsGrid.addComponentColumn(event -> {
            Div div = new Div();
            div.setText(event.getStatus().name());
            switch (event.getStatus()) {
                case DRAFT:
                    div.getStyle().set("color", "var(--lumo-secondary-text-color)");
                    break;
                case PUBLISHED:
                    div.getStyle().set("color", "var(--lumo-success-color)");
                    break;
                case CANCELLED:
                    div.getStyle().set("color", "var(--lumo-error-color)");
                    break;
                case FINISHED:
                    div.getStyle().set("color", "var(--lumo-tertiary-text-color)");
                    break;
            }
            div.getStyle().set("font-weight", "500");
            return div;
        })
        .setHeader("Status")
        .setAutoWidth(true);
        
        eventsGrid.addComponentColumn(event -> {
            com.vaadin.flow.component.orderedlayout.HorizontalLayout layout = new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
            layout.setSpacing(true);
            layout.setPadding(false);
            
            // View button
            Button viewButton = new Button("View");
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            RouterLink viewLink = new RouterLink("", com.xenplan.app.ui.view.publicview.EventDetailsView.class, 
                    new com.vaadin.flow.router.RouteParameters(
                            java.util.Map.of("eventId", event.getId().toString())));
            viewLink.add(viewButton);
            viewLink.getStyle().set("text-decoration", "none");
            
            // Publish button (only for DRAFT)
            if (event.getStatus() == EventStatus.DRAFT) {
                Button publishButton = new Button("Publish");
                publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                publishButton.addClickListener(e -> handlePublishEvent(event.getId()));
                layout.add(publishButton);
            }
            
            // Cancel button (for PUBLISHED)
            if (event.getStatus() == EventStatus.PUBLISHED) {
                Button cancelButton = new Button("Cancel");
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.addClickListener(e -> handleCancelEvent(event.getId()));
                layout.add(cancelButton);
            }
            
            layout.add(viewLink);
            return layout;
        })
        .setHeader("Actions")
        .setAutoWidth(true);
        
        add(eventsGrid);
    }

    private void loadEvents() {
        // Admin can see all events - use findAllWithOrganizer to eagerly load organizer
        List<Event> events = eventRepository.findAllWithOrganizer();
        eventsGrid.setItems(events);
        
        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("No events found.");
            noEvents.getStyle().set("color", "var(--lumo-secondary-text-color)");
            noEvents.getStyle().set("text-align", "center");
            noEvents.getStyle().set("padding", "2rem");
            add(noEvents);
        }
    }

    private void handlePublishEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog(
                "Publish Event",
                "Are you sure you want to publish this event?"
        );
        
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    eventService.publishEvent(eventId, currentUser);
                    Notification.show("Event published successfully", 5000, Notification.Position.MIDDLE);
                    loadEvents();
                } catch (ConflictException e) {
                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
                } catch (BusinessException e) {
                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
                } catch (Exception e) {
                    Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
                }
            }
        });
        
        dialog.open();
    }

    private void handleCancelEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog(
                "Cancel Event",
                "Are you sure you want to cancel this event?"
        );
        
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    eventService.cancelEvent(eventId, currentUser);
                    Notification.show("Event cancelled successfully", 5000, Notification.Position.MIDDLE);
                    loadEvents();
                } catch (ConflictException e) {
                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
                } catch (BusinessException e) {
                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
                } catch (Exception e) {
                    Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
                }
            }
        });
        
        dialog.open();
    }
}

