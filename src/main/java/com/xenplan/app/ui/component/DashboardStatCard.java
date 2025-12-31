package com.xenplan.app.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class DashboardStatCard extends Div {

    public DashboardStatCard(String title, long count, VaadinIcon icon, String colorClass) {
        this.addClassName("dashboard-card");
        this.setWidthFull();

        // Icon Header
        Icon iconComponent = icon.create();
        iconComponent.setSize("2rem");
        iconComponent.getStyle().set("color", colorClass);

        Div iconContainer = new Div(iconComponent);
        iconContainer.getStyle().set("padding", "0.75rem");
        iconContainer.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
        iconContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        iconContainer.getStyle().set("display", "inline-flex");
        iconContainer.getStyle().set("margin-bottom", "1rem");
        
        // Count
        H2 countText = new H2(String.valueOf(count));
        countText.getStyle().set("font-size", "var(--lumo-font-size-xxxl)");
        countText.getStyle().set("margin", "0.5rem 0");
        countText.getStyle().set("font-weight", "700");
        countText.getStyle().set("color", "var(--lumo-primary-color)");
        countText.getStyle().set("line-height", "1.2");

        // Label
        Span label = new Span(title);
        label.getStyle().set("color", "var(--lumo-secondary-text-color)");
        label.getStyle().set("font-weight", "600");
        label.getStyle().set("font-size", "var(--lumo-font-size-s)");
        label.getStyle().set("text-transform", "uppercase");
        label.getStyle().set("letter-spacing", "0.05em");

        VerticalLayout layout = new VerticalLayout(iconContainer, countText, label);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.START);
        layout.setWidthFull();
        
        add(layout);
    }
}
