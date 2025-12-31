package com.xenplan.app.ui.view.admin;

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
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.service.EventService;
import com.xenplan.app.service.UserService;
import com.xenplan.app.ui.component.DashboardStatCard;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.ui.view.UserProfileView;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@Route(value = "admin/dashboard", layout = MainLayout.class)
@PageTitle("Admin Dashboard | XenPlan")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final User currentUser;

    public AdminDashboardView(EventService eventService, EventRepository eventRepository, UserService userService) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        setupHeader();
        setupStats();
        setupQuickActions();
    }

    private void setupHeader() {
        H2 title = new H2("Platform Overview");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "2rem");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        title.getStyle().set("font-weight", "700");
        title.getStyle().set("text-align", "center");
        add(title);
    }

    private void setupStats() {
        List<Event> allEvents = eventRepository.findAll();
        List<User> allUsers = userService.findAll();
        
        long totalEvents = allEvents.size();
        long publishedEvents = allEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .count();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream()
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .count();
        
        // Create stat cards using the new component (NO EMOJIS - Vector Icons Only)
        DashboardStatCard eventsCard = new DashboardStatCard(
                "Total Events", 
                totalEvents, 
                VaadinIcon.CALENDAR, 
                "var(--lumo-primary-color)"
        );
        
        DashboardStatCard pubCard = new DashboardStatCard(
                "Published", 
                publishedEvents, 
                VaadinIcon.CHECK_CIRCLE, 
                "var(--lumo-success-color)"
        );
        
        DashboardStatCard usersCard = new DashboardStatCard(
                "Total Users", 
                totalUsers, 
                VaadinIcon.USERS, 
                "var(--lumo-error-color)"
        );
        
        DashboardStatCard activeCard = new DashboardStatCard(
                "Active Users", 
                activeUsers, 
                VaadinIcon.USER_CHECK, 
                "var(--lumo-primary-color)"
        );
        
        // Grid Layout for Cards (Responsive)
        Div statsGrid = new Div(eventsCard, pubCard, usersCard, activeCard);
        statsGrid.getStyle().set("display", "grid");
        statsGrid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))");
        statsGrid.getStyle().set("gap", "var(--lumo-space-l)");
        statsGrid.getStyle().set("width", "100%");
        statsGrid.getStyle().set("max-width", "1200px");
        statsGrid.getStyle().set("margin", "0 auto");
        
        add(statsGrid);
    }

    private void setupQuickActions() {
        // Quick Actions (Styled Buttons)
        Button manageUsersBtn = new Button("Manage Users", new Icon(VaadinIcon.USERS));
        manageUsersBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        manageUsersBtn.getStyle().set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1)");
        RouterLink usersLink = new RouterLink("", UserManagementView.class);
        usersLink.add(manageUsersBtn);
        usersLink.getStyle().set("text-decoration", "none");
        
        Button manageEventsBtn = new Button("Manage Events", new Icon(VaadinIcon.CALENDAR_CLOCK));
        manageEventsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        manageEventsBtn.getStyle().set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1)");
        RouterLink eventsLink = new RouterLink("", EventManagementView.class);
        eventsLink.add(manageEventsBtn);
        eventsLink.getStyle().set("text-decoration", "none");
        
        HorizontalLayout actions = new HorizontalLayout(usersLink, eventsLink);
        actions.setSpacing(true);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actions.getStyle().set("margin-top", "3rem");
        actions.setWidthFull();
        
        add(actions);
    }
}
