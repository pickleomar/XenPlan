package com.xenplan.app.ui.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.xenplan.app.domain.entity.Reservation;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.ReservationStatus;
import com.xenplan.app.security.SecurityUtils;
import com.xenplan.app.service.ReservationService;
import com.xenplan.app.ui.component.DashboardStatCard;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.ui.view.publicview.EventListView;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "client/dashboard", layout = MainLayout.class)
@RolesAllowed("CLIENT")
@PageTitle("Dashboard | XenPlan")
public class ClientDashboardView extends VerticalLayout {

    public ClientDashboardView(ReservationService reservationService) {
        addClassName("client-dashboard");
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(true);

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1200px");
        container.setWidthFull();
        container.setSpacing(true);

        H2 title = new H2("Welcome Back");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);
        Paragraph subtitle = new Paragraph("Overview of your upcoming events and booking history.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.LARGE);

        User currentUser = SecurityUtils.getCurrentUser();
        List<Reservation> reservations = reservationService.getReservationsByUser(currentUser);

        long activeBookings = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.PENDING)
                .count();
        
        long totalBookings = reservations.size();
        
        BigDecimal totalSpent = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Div statsGrid = new Div();
        statsGrid.setWidthFull();
        statsGrid.addClassNames(LumoUtility.Display.GRID, LumoUtility.Gap.LARGE);
        statsGrid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))");

        statsGrid.add(
            new DashboardStatCard("Active Bookings", String.valueOf(activeBookings), VaadinIcon.TICKET, DashboardStatCard.ColorVariant.BLUE),
            new DashboardStatCard("Total Reservations", String.valueOf(totalBookings), VaadinIcon.BOOK, DashboardStatCard.ColorVariant.PURPLE),
            new DashboardStatCard("Total Spent", "$" + totalSpent.toString(), VaadinIcon.WALLET, DashboardStatCard.ColorVariant.GREEN)
        );

        H2 actionsTitle = new H2("Quick Actions");
        actionsTitle.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Top.XLARGE);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button browseEventsBtn = new Button("Browse Events", new Icon(VaadinIcon.SEARCH));
        browseEventsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        browseEventsBtn.setHeight("50px");
        browseEventsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(EventListView.class)));

        Button myReservationsBtn = new Button("My Reservations", new Icon(VaadinIcon.LIST));
        myReservationsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        myReservationsBtn.setHeight("50px");
        myReservationsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyReservationsView.class)));

        actions.add(browseEventsBtn, myReservationsBtn);

        container.add(title, subtitle, statsGrid, actionsTitle, actions);
        add(container);
    }
}