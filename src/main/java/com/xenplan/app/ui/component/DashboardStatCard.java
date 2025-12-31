package com.xenplan.app.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class DashboardStatCard extends Div {

    public enum ColorVariant {
        BLUE("var(--lumo-primary-color)", "rgba(59, 130, 246, 0.1)"),
        GREEN("var(--lumo-success-color)", "rgba(16, 185, 129, 0.1)"),
        RED("var(--lumo-error-color)", "rgba(239, 68, 68, 0.1)"),
        PURPLE("#8b5cf6", "rgba(139, 92, 246, 0.1)");

        final String foreground;
        final String background;

        ColorVariant(String fg, String bg) {
            this.foreground = fg;
            this.background = bg;
        }
    }

    public DashboardStatCard(String title, String value, VaadinIcon icon, ColorVariant variant) {
        addClassName("dashboard-card");
        setWidthFull();

        // 1. Icon Box
        Icon i = icon.create();
        i.setSize("24px");
        i.setColor(variant.foreground);

        Div iconBox = new Div(i);
        iconBox.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
        iconBox.setWidth("48px");
        iconBox.setHeight("48px");
        iconBox.getStyle().set("background-color", variant.background);
        iconBox.getStyle().set("border-radius", "12px");
        iconBox.getStyle().set("margin-bottom", "1rem");

        // 2. Value
        H2 valueText = new H2(value);
        valueText.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.Margin.NONE);
        valueText.getStyle().set("color", "var(--lumo-header-text-color)");

        // 3. Label
        Span label = new Span(title);
        label.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextTransform.UPPERCASE);
        label.getStyle().set("color", "var(--lumo-secondary-text-color)");
        label.getStyle().set("letter-spacing", "0.05em");

        VerticalLayout layout = new VerticalLayout(iconBox, valueText, label);
        layout.setPadding(true);
        layout.setSpacing(false);
        
        // Inline styles to ensure it looks good even if CSS fails to load
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("border-radius", "16px");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        
        add(layout);
    }
}