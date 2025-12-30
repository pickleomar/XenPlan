package com.xenplan.app.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.service.EventService;
import com.xenplan.app.service.ReservationService;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class ReservationDialog extends Dialog {

    private final Event event;
    private final User user;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final IntegerField seatsField = new IntegerField("Number of Seats");
    private final TextArea commentField = new TextArea("Comment (Optional)");
    private final Button confirmButton = new Button("Confirm Reservation");
    private final Button cancelButton = new Button("Cancel");
    private final Paragraph totalPriceInfo = new Paragraph();
    
    private Consumer<Boolean> onReservationComplete;

    public ReservationDialog(Event event, User user, EventService eventService, ReservationService reservationService) {
        this.event = event;
        this.user = user;
        this.eventService = eventService;
        this.reservationService = reservationService;
        
        setWidth("500px");
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        
        setupDialog();
    }

    private void setupDialog() {
        H3 title = new H3("Reserve Seats");
        title.getStyle().set("margin-top", "0");
        
        Paragraph eventInfo = new Paragraph("Event: " + event.getTitle());
        eventInfo.getStyle().set("font-weight", "500");
        
        Integer availableSeats = eventService.calculateAvailableSeats(event.getId());
        Paragraph seatsInfo = new Paragraph("Available seats: " + availableSeats);
        seatsInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        BigDecimal unitPrice = event.getUnitPrice();
        Paragraph priceInfo = new Paragraph("Price per seat: " + formatPrice(unitPrice));
        priceInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        
        // Seats field
        seatsField.setRequired(true);
        seatsField.setMin(1);
        seatsField.setMax(10);
        seatsField.setValue(1);
        seatsField.setWidthFull();
        seatsField.setHelperText("Between 1 and 10 seats");
        seatsField.addValueChangeListener(e -> updateTotalPrice(totalPriceInfo));
        
        // Comment field
        commentField.setMaxLength(500);
        commentField.setWidthFull();
        commentField.setHeight("100px");
        commentField.setHelperText("Maximum 500 characters");
        
        formLayout.add(seatsField, 1);
        formLayout.add(commentField, 1);
        
        // Total price display
        totalPriceInfo.getStyle().set("font-size", "var(--lumo-font-size-l)");
        totalPriceInfo.getStyle().set("font-weight", "600");
        totalPriceInfo.getStyle().set("color", "var(--lumo-primary-color)");
        totalPriceInfo.getStyle().set("margin-top", "1rem");
        updateTotalPrice(totalPriceInfo);
        
        // Buttons
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(e -> handleReservation());
        
        cancelButton.addClickListener(e -> close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        
        add(title, eventInfo, seatsInfo, priceInfo, formLayout, totalPriceInfo, buttonLayout);
        
        // Store reference for update
        seatsField.addValueChangeListener(e -> updateTotalPrice(totalPriceInfo));
    }

    private void updateTotalPrice(Paragraph totalPriceInfo) {
        Integer seats = seatsField.getValue();
        if (seats != null && seats > 0) {
            BigDecimal total = event.getUnitPrice().multiply(BigDecimal.valueOf(seats));
            totalPriceInfo.setText("Total: " + formatPrice(total));
        } else {
            totalPriceInfo.setText("Total: " + formatPrice(BigDecimal.ZERO));
        }
    }

    private void handleReservation() {
        Integer seats = seatsField.getValue();
        String comment = commentField.getValue();
        
        if (seats == null || seats < 1 || seats > 10) {
            Notification.show("Please enter a valid number of seats (1-10)", 3000, Notification.Position.MIDDLE);
            seatsField.setInvalid(true);
            return;
        }
        
        confirmButton.setEnabled(false);
        
        try {
            reservationService.createReservation(
                    event.getId(),
                    seats,
                    comment != null && !comment.isEmpty() ? comment : null,
                    user
            );
            
            Notification.show("Reservation created successfully!", 5000, Notification.Position.MIDDLE);
            
            if (onReservationComplete != null) {
                onReservationComplete.accept(true);
            }
            
            close();
            
        } catch (ConflictException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
            seatsField.setInvalid(true);
        } catch (BusinessException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
        } finally {
            confirmButton.setEnabled(true);
        }
    }

    private String formatPrice(BigDecimal price) {
        return String.format("$%.2f", price);
    }

    public void setOnReservationComplete(Consumer<Boolean> callback) {
        this.onReservationComplete = callback;
    }
}
