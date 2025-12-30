package com.xenplan.app.ui.view.organizer;

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
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@Route(value = "organizer/dashboard", layout = MainLayout.class)
@PageTitle("Organizer Dashboard | XenPlan")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final User currentUser;

    public OrganizerDashboardView(EventService eventService) {
        this.eventService = eventService;
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
        H2 title = new H2("Organizer Dashboard");
        title.getStyle().set("margin-top", "0");
        
        Paragraph subtitle = new Paragraph("Manage your events and track reservations");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        
        add(title, subtitle);
    }

    private void setupStats() {
        List<Event> events = eventService.findByOrganizer(currentUser);
        
        long totalEvents = events.size();
        long draftEvents = events.stream()
                .filter(e -> e.getStatus() == EventStatus.DRAFT)
                .count();
        long publishedEvents = events.stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .count();
        long finishedEvents = events.stream()
                .filter(e -> e.getStatus() == EventStatus.FINISHED)
                .count();
        
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        
        statsLayout.add(createStatCard("Total Events", String.valueOf(totalEvents), "📅"));
        statsLayout.add(createStatCard("Draft", String.valueOf(draftEvents), "📝"));
        statsLayout.add(createStatCard("Published", String.valueOf(publishedEvents), "✅"));
        statsLayout.add(createStatCard("Finished", String.valueOf(finishedEvents), "🏁"));
        
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
        
        Button createEvent = new Button("Create Event");
        createEvent.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createEvent.setWidth("200px");
        RouterLink createLink = new RouterLink("", EventFormView.class, 
                new com.vaadin.flow.router.RouteParameters(
                        java.util.Map.of("eventId", "new")));
        createLink.add(createEvent);
        createLink.getStyle().set("text-decoration", "none");
        
        Button myEvents = new Button("My Events");
        myEvents.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        myEvents.setWidth("200px");
        RouterLink eventsLink = new RouterLink("", MyEventsView.class);
        eventsLink.add(myEvents);
        eventsLink.getStyle().set("text-decoration", "none");
        
        actionsLayout.add(createLink, eventsLink);
        add(actionsLayout);
    }
}
