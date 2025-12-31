package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    
    // We use a Div with CSS Grid instead of VerticalLayout for the cards
    private final Div eventsGrid = new Div();
    
    private final ComboBox<EventCategory> categoryFilter = new ComboBox<>();
    private final ComboBox<String> cityFilter = new ComboBox<>();
    private final Button clearFiltersButton = new Button("Clear", new Icon(VaadinIcon.ERASER));

    public EventListView(EventService eventService) {
        this.eventService = eventService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        setupHeader();
        setupFilters();
        setupEventsGrid();
        
        loadEvents();
    }

    /*private void setupHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        
        H2 title = new H2("Upcoming Events");
        title.getStyle().set("margin-bottom", "0.5rem");
        
        Paragraph subtitle = new Paragraph("Discover and reserve your favorite events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        
        header.add(title, subtitle);
        add(header);
    }*/
    private void setupHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull(); // 1. Make the container fill the screen width
        header.setPadding(false);
        header.setSpacing(false);
        
        // 2. Center the items horizontally inside the layout
        header.setAlignItems(Alignment.CENTER); 
        
        H2 title = new H2("Upcoming Events");
        title.getStyle().set("margin-bottom", "0.5rem");
        
        Paragraph subtitle = new Paragraph("Discover and reserve your favorite events");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        subtitle.getStyle().set("text-align", "center"); // 3. Ensure text centers if it wraps
        
        header.add(title, subtitle);
        add(header);
    }

    private void setupFilters() {
        // Create a styled container for the filters
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(FlexComponent.Alignment.CENTER); // Center vertically
        filterBar.setPadding(true);
        
        // Add a nice background to the filter bar
        filterBar.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        filterBar.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        filterBar.getStyle().set("margin-bottom", "1rem");

        // Category Filter
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setPlaceholder("Category");
        categoryFilter.setPrefixComponent(new Icon(VaadinIcon.TAGS));
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> applyFilters());
        
        // City Filter
        List<String> cities = eventService.findAllPublished().stream()
                .map(Event::getCity)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        cityFilter.setItems(cities);
        cityFilter.setPlaceholder("City");
        cityFilter.setPrefixComponent(new Icon(VaadinIcon.MAP_MARKER));
        cityFilter.setClearButtonVisible(true);
        cityFilter.addValueChangeListener(e -> applyFilters());
        
        // Button Styling
        clearFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFiltersButton.addClickListener(e -> {
            categoryFilter.clear();
            cityFilter.clear();
            loadEvents();
        });

        // Add to bar
        filterBar.add(categoryFilter, cityFilter);
        
        // Spacer to push the Clear button to the right
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        filterBar.add(spacer, clearFiltersButton);

        add(filterBar);
    }

    private void setupEventsGrid() {
        // CSS Grid Logic
        eventsGrid.setWidthFull();
        eventsGrid.getStyle().set("display", "grid");
        // This creates a responsive grid: columns are at least 350px wide, and fill the space
        eventsGrid.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))");
        eventsGrid.getStyle().set("gap", "1.5rem"); // Space between cards
        eventsGrid.getStyle().set("padding-bottom", "2rem");
        
        add(eventsGrid);
    }

    private void loadEvents() {
        eventsGrid.removeAll();
        
        List<Event> events;
        
        if (categoryFilter.getValue() != null && cityFilter.getValue() != null) {
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
        
        events = events.stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .collect(Collectors.toList());
        
        if (events.isEmpty()) {
            VerticalLayout emptyState = new VerticalLayout();
            emptyState.setAlignItems(Alignment.CENTER);
            emptyState.setPadding(true);
            
            Icon icon = new Icon(VaadinIcon.SEARCH);
            icon.setSize("48px");
            icon.setColor("var(--lumo-secondary-text-color)");
            
            Paragraph noEvents = new Paragraph("No events found. Try adjusting your filters.");
            noEvents.getStyle().set("color", "var(--lumo-secondary-text-color)");
            
            emptyState.add(icon, noEvents);
            // Span the empty state across all columns
            emptyState.getStyle().set("grid-column", "1 / -1"); 
            
            eventsGrid.add(emptyState);
        } else {
            for (Event event : events) {
                Integer availableSeats = eventService.calculateAvailableSeats(event.getId());
                EventCard card = new EventCard(event, availableSeats);
                // Card width is handled by the Grid now
                eventsGrid.add(card);
            }
        }
    }

    private void applyFilters() {
        loadEvents();
    }
}