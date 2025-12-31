package com.xenplan.app.ui.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
    private User currentUser;
    private Grid<Event> eventsGrid;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public MyEventsView(EventService eventService) {
        this.eventService = eventService;
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        
        try {
            this.currentUser = SecurityUtils.getCurrentUser();
            
            if (currentUser == null) {
                Paragraph errorMsg = new Paragraph("Authentication required. Please log in.");
                errorMsg.getStyle().set("color", "var(--lumo-error-color)");
                errorMsg.getStyle().set("text-align", "center");
                errorMsg.getStyle().set("padding", "2rem");
                add(errorMsg);
                return;
            }
            
            setupHeader();
            setupGrid();
            loadEvents();
        } catch (Exception e) {
            Paragraph errorMsg = new Paragraph("Error initializing view: " + e.getMessage());
            errorMsg.getStyle().set("color", "var(--lumo-error-color)");
            errorMsg.getStyle().set("text-align", "center");
            errorMsg.getStyle().set("padding", "2rem");
            add(errorMsg);
        }
    }

    private void setupHeader() {
        VerticalLayout headerLayout = new VerticalLayout();
        headerLayout.setSpacing(false);
        headerLayout.setPadding(false);
        headerLayout.setWidthFull();
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidthFull();
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);
        
        H2 title = new H2("My Events");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "0.25rem");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        title.getStyle().set("font-weight", "600");
        
        Paragraph subtitle = new Paragraph("Manage and organize your events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        subtitle.getStyle().set("margin-bottom", "0");
        
        titleSection.add(title, subtitle);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        RouterLink backLink = new RouterLink("", OrganizerDashboardView.class);
        backLink.getStyle().set("text-decoration", "none");
        Button backButton = new Button("Dashboard", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backLink.add(backButton);
        
        Button createButton = new Button("Create Event", new Icon(VaadinIcon.PLUS));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        RouterLink createLink = new RouterLink("", EventFormView.class);
        createLink.add(createButton);
        createLink.getStyle().set("text-decoration", "none");
        
        buttonLayout.add(backLink, createLink);
        
        titleLayout.add(titleSection, buttonLayout);
        headerLayout.add(titleLayout);
        
        add(headerLayout);
    }

    private void setupGrid() {
        eventsGrid = new Grid<>(Event.class, false);
        eventsGrid.setWidthFull();
        eventsGrid.setAllRowsVisible(true);
        eventsGrid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES);
        
        // Title column with styling
        eventsGrid.addColumn(Event::getTitle)
                .setHeader("Title")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(2);
        
        // Category column
        eventsGrid.addColumn(e -> e.getCategory().name())
                .setHeader("Category")
                .setSortable(true)
                .setAutoWidth(true);
        
        // Start Date column
        eventsGrid.addColumn(e -> e.getStartDate().format(DATE_FORMATTER))
                .setHeader("Start Date")
                .setSortable(true)
                .setAutoWidth(true);
        
        // Location column
        eventsGrid.addColumn(e -> e.getVenue() + ", " + e.getCity())
                .setHeader("Location")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        // Status column with badge styling
        eventsGrid.addComponentColumn(event -> {
            Div badge = new Div();
            badge.setText(event.getStatus().name());
            badge.getStyle().set("padding", "0.25rem 0.75rem");
            badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            badge.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            badge.getStyle().set("font-weight", "600");
            badge.getStyle().set("text-transform", "uppercase");
            badge.getStyle().set("letter-spacing", "0.05em");
            
            switch (event.getStatus()) {
                case DRAFT:
                    badge.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
                    badge.getStyle().set("color", "var(--lumo-secondary-text-color)");
                    break;
                case PUBLISHED:
                    badge.getStyle().set("background-color", "var(--lumo-success-color-10pct)");
                    badge.getStyle().set("color", "var(--lumo-success-color)");
                    break;
                case CANCELLED:
                    badge.getStyle().set("background-color", "var(--lumo-error-color-10pct)");
                    badge.getStyle().set("color", "var(--lumo-error-color)");
                    break;
                case FINISHED:
                    badge.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
                    badge.getStyle().set("color", "var(--lumo-tertiary-text-color)");
                    break;
            }
            return badge;
        })
        .setHeader("Status")
        .setAutoWidth(true);
        
        // Actions column with modern buttons
        eventsGrid.addComponentColumn(event -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setSpacing(true);
            layout.setPadding(false);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            
            // View button
            Button viewButton = new Button(new Icon(VaadinIcon.EYE));
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewButton.setTooltipText("View Event");
            RouterLink viewLink = new RouterLink("", com.xenplan.app.ui.view.publicview.EventDetailsView.class, 
                    new com.vaadin.flow.router.RouteParameters(
                            java.util.Map.of("eventId", event.getId().toString())));
            viewLink.add(viewButton);
            viewLink.getStyle().set("text-decoration", "none");
            layout.add(viewLink);
            
            // Edit button (for DRAFT and PUBLISHED - allow editing published events)
            if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.PUBLISHED) {
                Button editButton = new Button(new Icon(VaadinIcon.EDIT));
                editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                editButton.setTooltipText("Edit Event");
                RouterLink editLink = new RouterLink("", EventFormView.class, 
                        new com.vaadin.flow.router.RouteParameters(
                                java.util.Map.of("eventId", event.getId().toString())));
                editLink.add(editButton);
                editLink.getStyle().set("text-decoration", "none");
                layout.add(editLink);
            }
            
            // Publish button (only for DRAFT)
            if (event.getStatus() == EventStatus.DRAFT) {
                Button publishButton = new Button(new Icon(VaadinIcon.CHECK));
                publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                publishButton.setTooltipText("Publish Event");
                publishButton.addClickListener(e -> handlePublishEvent(event.getId()));
                layout.add(publishButton);
            }
            
            // Cancel button (for PUBLISHED)
            if (event.getStatus() == EventStatus.PUBLISHED) {
                Button cancelButton = new Button(new Icon(VaadinIcon.BAN));
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.setTooltipText("Cancel Event");
                cancelButton.addClickListener(e -> handleCancelEvent(event.getId()));
                layout.add(cancelButton);
            }
            
            // Delete button (for DRAFT and CANCELLED - allow deletion of cancelled events too)
            if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.CANCELLED) {
                Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                deleteButton.setTooltipText("Delete Event");
                deleteButton.addClickListener(e -> handleDeleteEvent(event.getId()));
                layout.add(deleteButton);
            }
            
            return layout;
        })
        .setHeader("Actions")
        .setAutoWidth(true)
        .setFlexGrow(0);
        
        add(eventsGrid);
    }

    private void loadEvents() {
        try {
            if (currentUser == null) {
                return;
            }
            // Use organizer ID instead of User entity to avoid detached entity issues
            List<Event> events = eventService.findByOrganizerId(currentUser.getId());
            eventsGrid.setItems(events);
            
            if (events.isEmpty()) {
                VerticalLayout emptyState = new VerticalLayout();
                emptyState.setAlignItems(FlexComponent.Alignment.CENTER);
                emptyState.setSpacing(true);
                emptyState.setPadding(true);
                emptyState.getStyle().set("margin-top", "2rem");
                
                Icon icon = new Icon(VaadinIcon.CALENDAR_O);
                icon.setSize("4rem");
                icon.getStyle().set("color", "var(--lumo-secondary-text-color)");
                
                Paragraph message = new Paragraph("You don't have any events yet.");
                message.getStyle().set("color", "var(--lumo-secondary-text-color)");
                message.getStyle().set("font-size", "var(--lumo-font-size-l)");
                
                Paragraph subMessage = new Paragraph("Create your first event to get started!");
                subMessage.getStyle().set("color", "var(--lumo-tertiary-text-color)");
                subMessage.getStyle().set("font-size", "var(--lumo-font-size-m)");
                
                Button createButton = new Button("Create Event", new Icon(VaadinIcon.PLUS));
                createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                RouterLink createLink = new RouterLink("", EventFormView.class);
                createLink.add(createButton);
                createLink.getStyle().set("text-decoration", "none");
                
                emptyState.add(icon, message, subMessage, createLink);
                add(emptyState);
            }
        } catch (Exception e) {
            Notification.show("Error loading events: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
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

