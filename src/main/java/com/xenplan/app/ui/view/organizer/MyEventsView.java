package com.xenplan.app.ui.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.component.ConfirmDialog;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.ui.view.publicview.EventDetailsView;
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
                add(new Paragraph("Authentication required. Please log in."));
                return;
            }
            
            setupHeader();
            setupGrid();
            loadEvents();
        } catch (Exception e) {
            add(new Paragraph("Error initializing view: " + e.getMessage()));
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
        title.getStyle().set("margin", "0 0 0.25rem 0");
        
        Paragraph subtitle = new Paragraph("Manage and organize your events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin", "0");
        
        titleSection.add(title, subtitle);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        RouterLink backLink = new RouterLink(OrganizerDashboardView.class);
        Button backButton = new Button("Dashboard", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backLink.add(backButton);
        
        // --- FIX: Pass "new" as parameter for Create Event ---
        // Without this parameter, the RouterLink crashes because the route expects :eventId
        RouterLink createLink = new RouterLink(EventFormView.class, new RouteParameters("eventId", "new"));
        Button createButton = new Button("Create Event", new Icon(VaadinIcon.PLUS));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
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
        eventsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        
        eventsGrid.addColumn(Event::getTitle).setHeader("Title").setSortable(true).setAutoWidth(true).setFlexGrow(2);
        eventsGrid.addColumn(e -> e.getCategory().name()).setHeader("Category").setSortable(true).setAutoWidth(true);
        eventsGrid.addColumn(e -> e.getStartDate().format(DATE_FORMATTER)).setHeader("Start Date").setSortable(true).setAutoWidth(true);
        eventsGrid.addColumn(e -> e.getVenue() + ", " + e.getCity()).setHeader("Location").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        
        eventsGrid.addComponentColumn(event -> {
            Div badge = new Div();
            badge.setText(event.getStatus().name());
            badge.getStyle().set("padding", "0.25rem 0.75rem");
            badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            badge.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            badge.getStyle().set("font-weight", "600");
            
            String color = switch (event.getStatus()) {
                case PUBLISHED -> "var(--lumo-success-color)";
                case CANCELLED -> "var(--lumo-error-color)";
                case FINISHED -> "var(--lumo-tertiary-text-color)";
                default -> "var(--lumo-secondary-text-color)"; // DRAFT
            };
            
            badge.getStyle().set("color", color);
            badge.getStyle().set("background-color", color + "1A"); // 10% opacity
            
            return badge;
        }).setHeader("Status").setAutoWidth(true);
        
        eventsGrid.addComponentColumn(event -> {
            HorizontalLayout layout = new HorizontalLayout();
            
            // View Link
            RouterLink viewLink = new RouterLink(EventDetailsView.class, new RouteParameters("eventId", event.getId().toString()));
            Button viewButton = new Button(new Icon(VaadinIcon.EYE));
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewLink.add(viewButton);
            layout.add(viewLink);
            
            // Edit Link (Draft/Published)
            if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.PUBLISHED) {
                RouterLink editLink = new RouterLink(EventFormView.class, new RouteParameters("eventId", event.getId().toString()));
                Button editButton = new Button(new Icon(VaadinIcon.EDIT));
                editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                editLink.add(editButton);
                layout.add(editLink);
            }
            
            // Publish (Draft only)
            if (event.getStatus() == EventStatus.DRAFT) {
                Button publishButton = new Button(new Icon(VaadinIcon.CHECK));
                publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                publishButton.addClickListener(e -> handlePublishEvent(event.getId()));
                layout.add(publishButton);
            }
            
            // Cancel (Published only)
            if (event.getStatus() == EventStatus.PUBLISHED) {
                Button cancelButton = new Button(new Icon(VaadinIcon.BAN));
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.addClickListener(e -> handleCancelEvent(event.getId()));
                layout.add(cancelButton);
            }
            
            // Delete (Draft/Cancelled)
            if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.CANCELLED) {
                Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                deleteButton.addClickListener(e -> handleDeleteEvent(event.getId()));
                layout.add(deleteButton);
            }
            
            return layout;
        }).setHeader("Actions").setAutoWidth(true);
        
        add(eventsGrid);
    }

    private void loadEvents() {
        try {
            if (currentUser == null) return;
            List<Event> events = eventService.findByOrganizerId(currentUser.getId());
            eventsGrid.setItems(events);
        } catch (Exception e) {
            Notification.show("Error loading events: " + e.getMessage());
        }
    }

    private void handlePublishEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog("Publish Event", "Are you sure? It will become visible to all.");
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    eventService.publishEvent(eventId, currentUser);
                    loadEvents();
                    Notification.show("Event published!");
                } catch (Exception e) {
                    Notification.show("Error: " + e.getMessage());
                }
            }
        });
        dialog.open();
    }

    private void handleCancelEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog("Cancel Event", "Are you sure?");
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    eventService.cancelEvent(eventId, currentUser);
                    loadEvents();
                    Notification.show("Event cancelled.");
                } catch (Exception e) {
                    Notification.show("Error: " + e.getMessage());
                }
            }
        });
        dialog.open();
    }

    private void handleDeleteEvent(UUID eventId) {
        ConfirmDialog dialog = new ConfirmDialog("Delete Event", "This cannot be undone.");
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    eventService.deleteEvent(eventId, currentUser);
                    loadEvents();
                    Notification.show("Event deleted.");
                } catch (Exception e) {
                    Notification.show("Error: " + e.getMessage());
                }
            }
        });
        dialog.open();
    }
}