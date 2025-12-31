package com.xenplan.app.ui.view.client;

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
import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.service.ReservationService;
import com.xenplan.app.ui.component.ConfirmDialog;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@Route(value = "client/reservations", layout = MainLayout.class)
@PageTitle("My Reservations | XenPlan")
@RolesAllowed("CLIENT")
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private final User currentUser;
    private Grid<Reservation> reservationsGrid;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public MyReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        
        setupHeader();
        setupGrid();
        loadReservations();
    }

    private void setupHeader() {
        H2 title = new H2("My Reservations");
        title.getStyle().set("margin-top", "0");
        
        RouterLink backLink = new RouterLink("← Back to Dashboard", ClientDashboardView.class);
        backLink.getStyle().set("text-decoration", "none");
        backLink.getStyle().set("margin-bottom", "1rem");
        
        add(title, backLink);
    }

    private void setupGrid() {
        reservationsGrid = new Grid<>(Reservation.class, false);
        reservationsGrid.setWidthFull();
        reservationsGrid.setAllRowsVisible(true);
        
        reservationsGrid.addColumn(Reservation::getReservationCode)
                .setHeader("Reservation Code")
                .setSortable(true)
                .setAutoWidth(true);
        
        reservationsGrid.addColumn(r -> r.getEvent().getTitle())
                .setHeader("Event")
                .setSortable(true)
                .setAutoWidth(true);
        
        reservationsGrid.addColumn(r -> r.getEvent().getStartDate().format(DATE_FORMATTER))
                .setHeader("Event Date")
                .setSortable(true)
                .setAutoWidth(true);
        
        reservationsGrid.addColumn(Reservation::getNumberOfSeats)
                .setHeader("Seats")
                .setSortable(true)
                .setAutoWidth(true);
        
        reservationsGrid.addColumn(r -> formatPrice(r.getTotalAmount()))
                .setHeader("Total Amount")
                .setSortable(true)
                .setAutoWidth(true);
        
        reservationsGrid.addColumn(r -> r.getReservationDate().format(DATE_FORMATTER))
                .setHeader("Reservation Date")
                .setSortable(true)
                .setAutoWidth(true);
        
        reservationsGrid.addComponentColumn(reservation -> {
            Div div = new Div();
            div.setText(reservation.getStatus().name());
            switch (reservation.getStatus()) {
                case PENDING:
                    div.getStyle().set("color", "var(--lumo-warning-color)");
                    break;
                case CONFIRMED:
                    div.getStyle().set("color", "var(--lumo-success-color)");
                    break;
                case CANCELLED:
                    div.getStyle().set("color", "var(--lumo-error-color)");
                    break;
            }
            div.getStyle().set("font-weight", "500");
            return div;
        })
        .setHeader("Status")
        .setAutoWidth(true);
        
        reservationsGrid.addComponentColumn(reservation -> {
            Button button = new Button();
            if (reservation.getStatus() != ReservationStatus.CANCELLED) {
                button.setText("Cancel");
                button.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                button.addClickListener(e -> handleCancelReservation(reservation));
            } else {
                button.setText("Cancelled");
                button.setEnabled(false);
            }
            return button;
        })
        .setHeader("Actions")
        .setAutoWidth(true);
        
        add(reservationsGrid);
    }

    private void loadReservations() {
        // Fix: Use the new service method that eagerly fetches Event details
        List<Reservation> reservations = reservationService.getUserReservations(currentUser);
        reservationsGrid.setItems(reservations);
        
        if (reservations.isEmpty()) {
            Paragraph noReservations = new Paragraph("You don't have any reservations yet.");
            noReservations.getStyle().set("color", "var(--lumo-secondary-text-color)");
            noReservations.getStyle().set("text-align", "center");
            noReservations.getStyle().set("padding", "2rem");
            add(noReservations);
        }
    }

    private void handleCancelReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog(
                "Cancel Reservation",
                "Are you sure you want to cancel this reservation? This action cannot be undone if the event starts within 48 hours."
        );
        
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    reservationService.cancelReservation(reservation.getId(), currentUser);
                    Notification.show("Reservation cancelled successfully", 5000, Notification.Position.MIDDLE);
                    loadReservations();
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

    private String formatPrice(BigDecimal price) {
        return String.format("$%.2f", price);
    }
}