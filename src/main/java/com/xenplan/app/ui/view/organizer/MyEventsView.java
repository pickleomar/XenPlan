package com.xenplan.app.ui.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.component.ConfirmDialog;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

@Route(value = "organizer/events", layout = MainLayout.class)
@PageTitle("My Events | XenPlan")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final User currentUser;
    private Grid<Event> eventsGrid;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public MyEventsView(EventService eventService) {
        this.eventService = eventService;
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
        H2 title = new H2("My Events");
        title.getStyle().set("margin-top", "0");
        
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        RouterLink backLink = new RouterLink("← Back to Dashboard", OrganizerDashboardView.class);
        backLink.getStyle().set("text-decoration", "none");
        
        Button createButton = new Button("Create Event");
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        RouterLink createLink = new RouterLink("", EventFormView.class);
        createLink.add(createButton);
        createLink.getStyle().set("text-decoration", "none");
        
        headerLayout.add(backLink, createLink);
        add(title, headerLayout);
    }

    private void setupGrid() {
        eventsGrid = new Grid<>(Event.class, false);
        eventsGrid.setWidthFull();
        eventsGrid.setAllRowsVisible(true);
        
        eventsGrid.addColumn(Event::getTitle)
                .setHeader("Title")
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
        
        eventsGrid.addColumn(e -> e.getVenue() + ", " + e.getCity())
                .setHeader("Location")
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
            HorizontalLayout layout = new HorizontalLayout();
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
            
            // Edit button (only for DRAFT)
            if (event.getStatus() == EventStatus.DRAFT) {
                Button editButton = new Button("Edit");
                editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                RouterLink editLink = new RouterLink("", EventFormView.class, 
                        new com.vaadin.flow.router.RouteParameters(
                                java.util.Map.of("eventId", event.getId().toString())));
                editLink.add(editButton);
                editLink.getStyle().set("text-decoration", "none");
                layout.add(editLink);
            }
            
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
            
            // Delete button (only for DRAFT with no reservations)
            if (event.getStatus() == EventStatus.DRAFT) {
                Button deleteButton = new Button("Delete");
                deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                deleteButton.addClickListener(e -> handleDeleteEvent(event.getId()));
                layout.add(deleteButton);
            }
            
            layout.add(viewLink);
            return layout;
        })
        .setHeader("Actions")
        .setAutoWidth(true);
        
        add(eventsGrid);
    }

    private void loadEvents() {
        List<Event> events = eventService.findByOrganizer(currentUser);
        eventsGrid.setItems(events);
        
        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("You don't have any events yet. Create your first event!");
            noEvents.getStyle().set("color", "var(--lumo-secondary-text-color)");
            noEvents.getStyle().set("text-align", "center");
            noEvents.getStyle().set("padding", "2rem");
            add(noEvents);
        }
    }

    private void handlePublishEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog(
                "Publish Event",
                "Are you sure you want to publish this event? It will become visible to all users."
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
                "Are you sure you want to cancel this event? Existing reservations will remain, but no new reservations can be made."
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

    private void handleDeleteEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog(
                "Delete Event",
                "Are you sure you want to delete this event? This action cannot be undone. Events with reservations cannot be deleted."
        );
        
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    eventService.deleteEvent(eventId, currentUser);
                    Notification.show("Event deleted successfully", 5000, Notification.Position.MIDDLE);
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

