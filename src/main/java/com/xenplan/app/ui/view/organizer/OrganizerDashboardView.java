package com.xenplan.app.ui.view.organizer;

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
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.repository.EventRepository;
import com.xenplan.app.security.SecurityUtils;
import com.xenplan.app.ui.component.DashboardStatCard;
import com.xenplan.app.ui.layout.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.Map;

@Route(value = "organizer/dashboard", layout = MainLayout.class)
@RolesAllowed({"ORGANIZER", "ADMIN"})
@PageTitle("Organizer Dashboard | XenPlan")
public class OrganizerDashboardView extends VerticalLayout {

    public OrganizerDashboardView(EventRepository eventRepository) {
        addClassName("organizer-dashboard");
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setPadding(true);

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1200px");
        container.setWidthFull();
        container.setSpacing(true);

        H2 title = new H2("Organizer Portal");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);
        Paragraph subtitle = new Paragraph("Manage your events and track performance.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.LARGE);

        User currentUser = SecurityUtils.getCurrentUser();
        long totalEvents = eventRepository.findByOrganizerOrderByCreatedAtDesc(currentUser).size();
        long published = eventRepository.findByOrganizerOrderByCreatedAtDesc(currentUser).stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED).count();
        long drafts = eventRepository.findByOrganizerOrderByCreatedAtDesc(currentUser).stream()
                .filter(e -> e.getStatus() == EventStatus.DRAFT).count();

        Div statsGrid = new Div();
        statsGrid.setWidthFull();
        statsGrid.addClassNames(LumoUtility.Display.GRID, LumoUtility.Gap.LARGE);
        statsGrid.getStyle().set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))");

        statsGrid.add(
            new DashboardStatCard("My Events", String.valueOf(totalEvents), VaadinIcon.CALENDAR_USER, DashboardStatCard.ColorVariant.BLUE),
            new DashboardStatCard("Live Now", String.valueOf(published), VaadinIcon.SIGNAL, DashboardStatCard.ColorVariant.GREEN),
            new DashboardStatCard("Drafts", String.valueOf(drafts), VaadinIcon.EDIT, DashboardStatCard.ColorVariant.PURPLE)
        );

        H2 actionsTitle = new H2("Quick Actions");
        actionsTitle.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Top.XLARGE);

        Button createBtn = new Button("Create New Event", new Icon(VaadinIcon.PLUS));
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.setHeight("50px");
        createBtn.addClickListener(e -> getUI().ifPresent(ui -> 
            ui.navigate(EventFormView.class, new com.vaadin.flow.router.RouteParameters(Map.of("eventId", "new")))));

        Button myEventsBtn = new Button("View My Events", new Icon(VaadinIcon.LIST));
        myEventsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        myEventsBtn.setHeight("50px");
        myEventsBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(MyEventsView.class)));

        HorizontalLayout actions = new HorizontalLayout(createBtn, myEventsBtn);

        container.add(title, subtitle, statsGrid, actionsTitle, actions);
        add(container);
    }
}