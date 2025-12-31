package com.xenplan.app.ui.view.admin;

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
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.repository.UserRepository;
import com.xenplan.app.ui.component.DashboardStatCard;
import com.xenplan.app.ui.layout.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/dashboard", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Admin Dashboard | XenPlan")
public class AdminDashboardView extends VerticalLayout {

    public AdminDashboardView(UserRepository userRepository, EventRepository eventRepository) {
        addClassName("admin-dashboard");
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(true);

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1200px");
        container.setWidthFull();
        container.setSpacing(true);

        H2 title = new H2("Platform Overview");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);
        Paragraph subtitle = new Paragraph("Real-time metrics and system performance.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.LARGE);

        String totalEvents = String.valueOf(eventRepository.count());
        String publishedEvents = String.valueOf(eventRepository.findByStatusOrderByStartDateAsc(EventStatus.PUBLISHED).size());
        String totalUsers = String.valueOf(userRepository.count());
        String activeUsers = String.valueOf(userRepository.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count());

        Div statsGrid = new Div();
        statsGrid.setWidthFull();
        statsGrid.addClassNames(LumoUtility.Display.GRID, LumoUtility.Gap.LARGE);
        statsGrid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))");

        statsGrid.add(
            new DashboardStatCard("Total Events", totalEvents, VaadinIcon.CALENDAR, DashboardStatCard.ColorVariant.BLUE),
            new DashboardStatCard("Published", publishedEvents, VaadinIcon.CHECK_CIRCLE, DashboardStatCard.ColorVariant.GREEN),
            new DashboardStatCard("Total Users", totalUsers, VaadinIcon.USERS, DashboardStatCard.ColorVariant.PURPLE),
            new DashboardStatCard("Active Accounts", activeUsers, VaadinIcon.USER_CHECK, DashboardStatCard.ColorVariant.RED)
        );

        H2 actionsTitle = new H2("Management");
        actionsTitle.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Top.XLARGE);

        Button manageUsersBtn = new Button("Manage Users", new Icon(VaadinIcon.USERS));
        manageUsersBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        manageUsersBtn.setHeight("50px");
        manageUsersBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(UserManagementView.class)));

        Button manageEventsBtn = new Button("Manage Events", new Icon(VaadinIcon.CALENDAR_CLOCK));
        manageEventsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        manageEventsBtn.setHeight("50px");
        manageEventsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(EventManagementView.class)));

        HorizontalLayout actions = new HorizontalLayout(manageUsersBtn, manageEventsBtn);

        container.add(title, subtitle, statsGrid, actionsTitle, actions);
        add(container);
    }
}