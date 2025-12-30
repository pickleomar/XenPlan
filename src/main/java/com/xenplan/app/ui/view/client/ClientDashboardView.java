package com.xenplan.app.ui.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.service.ReservationService;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.math.BigDecimal;
import java.util.List;

@Route(value = "client/dashboard", layout = MainLayout.class)
@PageTitle("Client Dashboard | XenPlan")
@RolesAllowed("CLIENT")
public class ClientDashboardView extends VerticalLayout {

    private final ReservationService reservationService;
    private final User currentUser;

    public ClientDashboardView(ReservationService reservationService) {
        this.reservationService = reservationService;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        
        setupHeader();
        setupStats();
        setupQuickActions();
    }

    private void setupHeader() {
        H2 title = new H2("Welcome, " + currentUser.getFirstName() + "!");
        title.getStyle().set("margin-top", "0");
        
        Paragraph subtitle = new Paragraph("Manage your reservations and discover new events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        
        add(title, subtitle);
    }

    private void setupStats() {
        List<Reservation> reservations = reservationService.getReservationsByUser(currentUser);
        
        long totalReservations = reservations.size();
        long pendingReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .count();
        long confirmedReservations = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count();
        
        BigDecimal totalSpent = reservations.stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        
        statsLayout.add(createStatCard("Total Reservations", String.valueOf(totalReservations), "📋"));
        statsLayout.add(createStatCard("Pending", String.valueOf(pendingReservations), "⏳"));
        statsLayout.add(createStatCard("Confirmed", String.valueOf(confirmedReservations), "✅"));
        statsLayout.add(createStatCard("Total Spent", formatPrice(totalSpent), "💰"));
        
        add(statsLayout);
    }

    private VerticalLayout createStatCard(String label, String value, String icon) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle().set("background", "var(--lumo-contrast-5pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.setWidth("100%");
        
        Div iconDiv = new Div();
        iconDiv.setText(icon);
        iconDiv.getStyle().set("font-size", "2rem");
        iconDiv.getStyle().set("margin-bottom", "0.5rem");
        
        Div valueDiv = new Div();
        valueDiv.setText(value);
        valueDiv.getStyle().set("font-size", "var(--lumo-font-size-xxl)");
        valueDiv.getStyle().set("font-weight", "600");
        valueDiv.getStyle().set("color", "var(--lumo-primary-color)");
        
        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.getStyle().set("font-size", "var(--lumo-font-size-s)");
        labelDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
        labelDiv.getStyle().set("margin-top", "0.5rem");
        
        card.add(iconDiv, valueDiv, labelDiv);
        return card;
    }

    private void setupQuickActions() {
        H3 actionsTitle = new H3("Quick Actions");
        actionsTitle.getStyle().set("margin-top", "2rem");
        add(actionsTitle);
        
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);
        actionsLayout.setWidthFull();
        
        Button viewReservations = new Button("My Reservations");
        viewReservations.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewReservations.setWidth("200px");
        RouterLink reservationsLink = new RouterLink("", MyReservationsView.class);
        reservationsLink.add(viewReservations);
        reservationsLink.getStyle().set("text-decoration", "none");
        
        Button browseEvents = new Button("Browse Events");
        browseEvents.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        browseEvents.setWidth("200px");
        RouterLink eventsLink = new RouterLink("", com.xenplan.app.ui.view.publicview.EventListView.class);
        eventsLink.add(browseEvents);
        eventsLink.getStyle().set("text-decoration", "none");
        
        actionsLayout.add(reservationsLink, eventsLink);
        add(actionsLayout);
    }

    private String formatPrice(BigDecimal price) {
        return String.format("$%.2f", price);
    }
}
