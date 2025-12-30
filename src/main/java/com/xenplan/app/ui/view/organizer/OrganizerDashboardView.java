package com.xenplan.app.ui.view.organizer;

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
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.ui.view.UserProfileView;
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
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setSpacing(false);
        titleLayout.setPadding(false);
        
        H2 title = new H2("Organizer Dashboard");
        title.getStyle().set("margin-top", "0");
        title.getStyle().set("margin-bottom", "0.25rem");
        title.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        title.getStyle().set("font-weight", "600");
        
        Paragraph subtitle = new Paragraph("Manage your events and track reservations");
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
        
        // Create Event - Prominent button
        Button createEvent = new Button("Create New Event", new Icon(VaadinIcon.PLUS_CIRCLE));
        createEvent.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        createEvent.setWidth("240px");
        createEvent.getStyle().set("height", "60px");
        createEvent.getStyle().set("font-weight", "600");
        RouterLink createLink = new RouterLink("", EventFormView.class, 
                new com.vaadin.flow.router.RouteParameters(
                        java.util.Map.of("eventId", "new")));
        createLink.add(createEvent);
        createLink.getStyle().set("text-decoration", "none");
        
        Button myEvents = new Button("My Events", new Icon(VaadinIcon.CALENDAR));
        myEvents.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        myEvents.setWidth("220px");
        myEvents.getStyle().set("height", "60px");
        RouterLink eventsLink = new RouterLink("", MyEventsView.class);
        eventsLink.add(myEvents);
        eventsLink.getStyle().set("text-decoration", "none");
        
        Button profileButton = new Button("My Profile", new Icon(VaadinIcon.USER));
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        profileButton.setWidth("220px");
        profileButton.getStyle().set("height", "60px");
        RouterLink profileLink = new RouterLink("", UserProfileView.class);
        profileLink.add(profileButton);
        profileLink.getStyle().set("text-decoration", "none");
        
        actionsLayout.add(createLink, eventsLink, profileLink);
        add(actionsLayout);
    }
}
