package com.xenplan.app.ui.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import com.xenplan.app.ui.view.UserProfileView;
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
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setSpacing(false);
        titleLayout.setPadding(false);
        
        H2 title = new H2("Welcome, " + currentUser.getFirstName() + "!");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "0.25rem");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        title.getStyle().set("font-weight", "600");
        
        Paragraph subtitle = new Paragraph("Manage your reservations and discover new events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        subtitle.getStyle().set("margin-bottom", "0");
        
        titleLayout.add(title, subtitle);
        
        // Profile button
        Button profileButton = new Button("My Profile", new Icon(VaadinIcon.USER));
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        RouterLink profileLink = new RouterLink("", UserProfileView.class);
        profileLink.add(profileButton);
        profileLink.getStyle().set("text-decoration", "none");
        
        headerLayout.add(titleLayout, profileLink);
        add(headerLayout);
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
        card.getStyle().set("background", "linear-gradient(135deg, var(--lumo-primary-color-5pct) 0%, var(--lumo-contrast-5pct) 100%)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        card.getStyle().set("transition", "transform 0.2s, box-shadow 0.2s");
        card.setWidth("100%");
        card.addClassName("stat-card");
        
        // Add hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("transform", "translateY(-4px)");
            card.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("transform", "translateY(0)");
            card.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        });
        
        Div iconDiv = new Div();
        iconDiv.setText(icon);
        iconDiv.getStyle().set("font-size", "2.5rem");
        iconDiv.getStyle().set("margin-bottom", "0.75rem");
        iconDiv.getStyle().set("opacity", "0.9");
        
        Div valueDiv = new Div();
        valueDiv.setText(value);
        valueDiv.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        valueDiv.getStyle().set("font-weight", "700");
        valueDiv.getStyle().set("color", "var(--lumo-primary-color)");
        valueDiv.getStyle().set("line-height", "1.2");
        
        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.getStyle().set("font-size", "var(--lumo-font-size-m)");
        labelDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
        labelDiv.getStyle().set("margin-top", "0.5rem");
        labelDiv.getStyle().set("font-weight", "500");
        
        card.add(iconDiv, valueDiv, labelDiv);
        return card;
    }

    private void setupQuickActions() {
        H3 actionsTitle = new H3("Quick Actions");
        actionsTitle.getStyle().set("margin-top", "2rem");
        actionsTitle.getStyle().set("margin-bottom", "1rem");
        actionsTitle.getStyle().set("font-weight", "600");
        add(actionsTitle);
        
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);
        actionsLayout.setWidthFull();
        actionsLayout.getStyle().set("flex-wrap", "wrap");
        
        Button viewReservations = new Button("My Reservations", new Icon(VaadinIcon.LIST));
        viewReservations.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        viewReservations.setWidth("220px");
        viewReservations.getStyle().set("height", "60px");
        RouterLink reservationsLink = new RouterLink("", MyReservationsView.class);
        reservationsLink.add(viewReservations);
        reservationsLink.getStyle().set("text-decoration", "none");
        
        Button browseEvents = new Button("Browse Events", new Icon(VaadinIcon.CALENDAR));
        browseEvents.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        browseEvents.setWidth("220px");
        browseEvents.getStyle().set("height", "60px");
        RouterLink eventsLink = new RouterLink("", com.xenplan.app.ui.view.publicview.EventListView.class);
        eventsLink.add(browseEvents);
        eventsLink.getStyle().set("text-decoration", "none");
        
        Button profileButton = new Button("My Profile", new Icon(VaadinIcon.USER));
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        profileButton.setWidth("220px");
        profileButton.getStyle().set("height", "60px");
        RouterLink profileLink = new RouterLink("", UserProfileView.class);
        profileLink.add(profileButton);
        profileLink.getStyle().set("text-decoration", "none");
        
        actionsLayout.add(reservationsLink, eventsLink, profileLink);
        add(actionsLayout);
    }

    private String formatPrice(BigDecimal price) {
        return String.format("$%.2f", price);
    }
}
