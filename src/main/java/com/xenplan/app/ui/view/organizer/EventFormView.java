package com.xenplan.app.ui.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Route(value = "organizer/events/:eventId", layout = MainLayout.class)
@PageTitle("Event Form | XenPlan")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class EventFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final User currentUser;
    private final BeanValidationBinder<Event> binder = new BeanValidationBinder<>(Event.class);
    
    private UUID eventId;
    private boolean isEditMode = false;
    private H2 titleComponent;
    
    private final TextField titleField = new TextField("Title");
    private final TextArea descriptionField = new TextArea("Description");
    private final ComboBox<EventCategory> categoryField = new ComboBox<>("Category");
    private final DateTimePicker startDateField = new DateTimePicker("Start Date & Time");
    private final DateTimePicker endDateField = new DateTimePicker("End Date & Time");
    private final TextField venueField = new TextField("Venue");
    private final TextField cityField = new TextField("City");
    private final IntegerField maxCapacityField = new IntegerField("Max Capacity");
    private final BigDecimalField unitPriceField = new BigDecimalField("Unit Price");
    private final TextField imageUrlField = new TextField("Image URL (Optional)");
    
    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    public EventFormView(EventService eventService) {
        this.eventService = eventService;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        setMaxWidth("800px");
        
        setupForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String eventIdParam = event.getRouteParameters().get("eventId").orElse(null);
        
        if (eventIdParam != null && !eventIdParam.isEmpty() && !eventIdParam.equals("new")) {
            try {
                this.eventId = UUID.fromString(eventIdParam);
                this.isEditMode = true;
                if (titleComponent != null) {
                    titleComponent.setText("Edit Event");
                }
                loadEvent();
            } catch (IllegalArgumentException e) {
                event.rerouteToError(NotFoundException.class);
            }
        } else {
            this.isEditMode = false;
            if (titleComponent != null) {
                titleComponent.setText("Create Event");
            }
        }
    }

    private void setupForm() {
        titleComponent = new H2(isEditMode ? "Edit Event" : "Create Event");
        titleComponent.getStyle().set("margin-top", "0");
        
        RouterLink backLink = new RouterLink("← Back to My Events", MyEventsView.class);
        backLink.getStyle().set("text-decoration", "none");
        backLink.getStyle().set("margin-bottom", "1rem");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setWidthFull();
        
        // Configure fields
        titleField.setRequired(true);
        titleField.setMaxLength(100);
        titleField.setWidthFull();
        
        descriptionField.setMaxLength(1000);
        descriptionField.setWidthFull();
        descriptionField.setHeight("100px");
        
        categoryField.setItems(EventCategory.values());
        categoryField.setRequired(true);
        categoryField.setWidthFull();
        
        startDateField.setRequiredIndicatorVisible(true);
        startDateField.setWidthFull();
        startDateField.setMin(LocalDateTime.now());
        
        endDateField.setRequiredIndicatorVisible(true);
        endDateField.setWidthFull();
        endDateField.setMin(LocalDateTime.now());
        
        venueField.setRequired(true);
        venueField.setMaxLength(100);
        venueField.setWidthFull();
        
        cityField.setRequired(true);
        cityField.setMaxLength(50);
        cityField.setWidthFull();
        
        maxCapacityField.setRequired(true);
        maxCapacityField.setMin(1);
        maxCapacityField.setWidthFull();
        
        unitPriceField.setRequiredIndicatorVisible(true);
        unitPriceField.setWidthFull();
        unitPriceField.setPrefixComponent(new com.vaadin.flow.component.html.Span("$"));
        
        imageUrlField.setMaxLength(255);
        imageUrlField.setWidthFull();
        
        // Add fields to form
        formLayout.add(titleField, 2);
        formLayout.add(descriptionField, 2);
        formLayout.add(categoryField, 1);
        formLayout.add(startDateField, 1);
        formLayout.add(endDateField, 1);
        formLayout.add(venueField, 1);
        formLayout.add(cityField, 1);
        formLayout.add(maxCapacityField, 1);
        formLayout.add(unitPriceField, 1);
        formLayout.add(imageUrlField, 2);
        
        // Configure binder
        binder.forField(titleField)
                .asRequired("Title is required")
                .bind(Event::getTitle, Event::setTitle);
        
        binder.forField(descriptionField)
                .bind(Event::getDescription, Event::setDescription);
        
        binder.forField(categoryField)
                .asRequired("Category is required")
                .bind(Event::getCategory, Event::setCategory);
        
        binder.forField(startDateField)
                .asRequired("Start date is required")
                .bind(Event::getStartDate, Event::setStartDate);
        
        binder.forField(endDateField)
                .asRequired("End date is required")
                .bind(Event::getEndDate, Event::setEndDate);
        
        binder.forField(venueField)
                .asRequired("Venue is required")
                .bind(Event::getVenue, Event::setVenue);
        
        binder.forField(cityField)
                .asRequired("City is required")
                .bind(Event::getCity, Event::setCity);
        
        binder.forField(maxCapacityField)
                .asRequired("Max capacity is required")
                .bind(Event::getMaxCapacity, Event::setMaxCapacity);
        
        binder.forField(unitPriceField)
                .asRequired("Unit price is required")
                .bind(Event::getUnitPrice, Event::setUnitPrice);
        
        binder.forField(imageUrlField)
                .bind(Event::getImageUrl, Event::setImageUrl);
        
        // Buttons
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> handleSave());
        
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyEventsView.class)));
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        
        add(titleComponent, backLink, formLayout, buttonLayout);
    }

    private void loadEvent() {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        
        // Check permissions
        boolean isCreator = event.getOrganizer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        
        if (!isCreator && !isAdmin) {
            Notification.show("You don't have permission to edit this event", 5000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));
            return;
        }
        
        // Check if event can be edited
        if (event.getStatus() == com.xenplan.app.domain.enums.EventStatus.PUBLISHED || 
            event.getStatus() == com.xenplan.app.domain.enums.EventStatus.FINISHED) {
            Notification.show("Cannot edit PUBLISHED or FINISHED events", 5000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));
            return;
        }
        
        binder.readBean(event);
    }

    private void handleSave() {
        Event event;
        
        if (isEditMode) {
            event = eventService.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
        } else {
            event = new Event();
        }
        
        try {
            binder.writeBean(event);
        } catch (ValidationException e) {
            Notification.show("Please fix the validation errors", 3000, Notification.Position.MIDDLE);
            return;
        }
        
        saveButton.setEnabled(false);
        
        try {
            if (isEditMode) {
                eventService.updateEvent(eventId, event, currentUser);
                Notification.show("Event updated successfully", 5000, Notification.Position.MIDDLE);
            } else {
                eventService.createEvent(event, currentUser);
                Notification.show("Event created successfully", 5000, Notification.Position.MIDDLE);
            }
            
            getUI().ifPresent(ui -> ui.navigate(MyEventsView.class));
            
        } catch (ConflictException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (BusinessException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
        } finally {
            saveButton.setEnabled(true);
        }
    }
}

