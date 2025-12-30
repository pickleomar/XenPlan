package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.xenplan.app.domain.entity.Event;
import com.xenplan.app.domain.enums.EventCategory;
import com.xenplan.app.domain.enums.EventStatus;
import com.xenplan.app.service.EventService;
import com.xenplan.app.ui.component.EventCard;
import com.xenplan.app.ui.layout.MainLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Events | XenPlan")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final VerticalLayout eventsContainer = new VerticalLayout();
    private final ComboBox<EventCategory> categoryFilter = new ComboBox<>("Category");
    private final ComboBox<String> cityFilter = new ComboBox<>("City");
    private final Button clearFiltersButton = new Button("Clear Filters");

    public EventListView(EventService eventService) {
        this.eventService = eventService;
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        
        setupHeader();
        setupFilters();
        setupEventsContainer();
        
        loadEvents();
    }

    private void setupHeader() {
        H2 title = new H2("Upcoming Events");
        title.getStyle().set("margin-top", "0");
        
        Paragraph subtitle = new Paragraph("Discover and reserve your favorite events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        
        add(title, subtitle);
    }

    private void setupFilters() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(FlexComponent.Alignment.END);
        filterLayout.setSpacing(true);
        
        // Category filter
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setPlaceholder("All Categories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("200px");
        categoryFilter.addValueChangeListener(e -> applyFilters());
        
        // City filter - get unique cities from events
        List<String> cities = eventService.findAllPublished().stream()
                .map(Event::getCity)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        cityFilter.setItems(cities);
        cityFilter.setPlaceholder("All Cities");
        cityFilter.setClearButtonVisible(true);
        cityFilter.setWidth("200px");
        cityFilter.addValueChangeListener(e -> applyFilters());
        
        // Clear filters button
        clearFiltersButton.addThemeVariants();
        clearFiltersButton.addClickListener(e -> {
            categoryFilter.clear();
            cityFilter.clear();
            loadEvents();
        });
        
        filterLayout.add(categoryFilter, cityFilter, clearFiltersButton);
        filterLayout.expand(categoryFilter, cityFilter);
        
        add(filterLayout);
    }

    private void setupEventsContainer() {
        eventsContainer.setPadding(false);
        eventsContainer.setSpacing(true);
        eventsContainer.setWidthFull();
        add(eventsContainer);
    }

    private void loadEvents() {
        eventsContainer.removeAll();
        
        List<Event> events;
        
        if (categoryFilter.getValue() != null && cityFilter.getValue() != null) {
            // Filter by both category and city
            List<Event> categoryEvents = eventService.findByCategory(categoryFilter.getValue());
            events = categoryEvents.stream()
                    .filter(e -> e.getCity().equals(cityFilter.getValue()))
                    .collect(Collectors.toList());
        } else if (categoryFilter.getValue() != null) {
            events = eventService.findByCategory(categoryFilter.getValue());
        } else if (cityFilter.getValue() != null) {
            events = eventService.findByCity(cityFilter.getValue());
        } else {
            events = eventService.findAllPublished();
        }
        
        // Filter out cancelled and finished events for public view
        events = events.stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .collect(Collectors.toList());
        
        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("No events found. Try adjusting your filters.");
            noEvents.getStyle().set("color", "var(--lumo-secondary-text-color)");
            noEvents.getStyle().set("text-align", "center");
            noEvents.getStyle().set("padding", "2rem");
            eventsContainer.add(noEvents);
        } else {
            // Create event cards in a grid-like layout
            for (Event event : events) {
                Integer availableSeats = eventService.calculateAvailableSeats(event.getId());
                EventCard card = new EventCard(event, availableSeats);
                card.setWidth("100%");
                eventsContainer.add(card);
            }
        }
    }

    private void applyFilters() {
        loadEvents();
    }
}
