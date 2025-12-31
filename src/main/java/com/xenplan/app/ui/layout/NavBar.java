package com.xenplan.app.ui.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.Lumo;
import com.xenplan.app.ui.view.publicview.EventListView;
import com.xenplan.app.ui.view.client.ClientDashboardView;
import com.xenplan.app.ui.view.organizer.OrganizerDashboardView;
import com.xenplan.app.ui.view.admin.AdminDashboardView;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

public class NavBar extends HorizontalLayout {

    public NavBar() {
        setWidthFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("background-color", "var(--lumo-base-color)");

        // Logo
        H1 title = new H1("XenPlan");
        title.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        title.getStyle().set("cursor", "pointer");
        title.getStyle().set("font-weight", "700");
        title.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        title.getStyle().set("margin", "0");

        HorizontalLayout menu = new HorizontalLayout();
        menu.setSpacing(true);
        menu.setAlignItems(FlexComponent.Alignment.CENTER);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                !auth.getName().equals("anonymousUser");

        // Dark Mode Toggle
        Button themeToggle = new Button();
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        themeToggle.setIcon(new Icon(VaadinIcon.MOON));
        themeToggle.setTooltipText("Toggle Dark Mode");
        themeToggle.addClickListener(e -> {
            UI currentUI = UI.getCurrent();
            if (currentUI != null) {
                com.vaadin.flow.dom.ThemeList themeList = currentUI.getElement().getThemeList();
                if (themeList.contains(Lumo.DARK)) {
                    themeList.remove(Lumo.DARK);
                    themeToggle.setIcon(new Icon(VaadinIcon.SUN_O));
                    themeToggle.setTooltipText("Enable Dark Mode");
                } else {
                    themeList.add(Lumo.DARK);
                    themeToggle.setIcon(new Icon(VaadinIcon.MOON));
                    themeToggle.setTooltipText("Disable Dark Mode");
                }
            }
        });
        
        // Check initial theme state
        UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getElement().getThemeList().contains(Lumo.DARK)) {
            themeToggle.setIcon(new Icon(VaadinIcon.MOON));
        } else {
            themeToggle.setIcon(new Icon(VaadinIcon.SUN_O));
        }
        
        menu.add(themeToggle);

        // Public link
        RouterLink eventsLink = new RouterLink("Events", EventListView.class);
        eventsLink.getStyle().set("text-decoration", "none");
        menu.add(eventsLink);

        if (isAuthenticated) {
            // Role-based links
            if (hasRole("CLIENT")) {
                RouterLink clientLink = new RouterLink("Dashboard", ClientDashboardView.class);
                clientLink.getStyle().set("text-decoration", "none");
                menu.add(clientLink);
            }

            if (hasRole("ORGANIZER")) {
                RouterLink organizerLink = new RouterLink("Organizer", OrganizerDashboardView.class);
                organizerLink.getStyle().set("text-decoration", "none");
                menu.add(organizerLink);
            }

            if (hasRole("ADMIN")) {
                RouterLink adminLink = new RouterLink("Admin", AdminDashboardView.class);
                adminLink.getStyle().set("text-decoration", "none");
                menu.add(adminLink);
            }

            Button logout = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT), e -> logout());
            logout.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            menu.add(logout);
        } else {
            // Show login/register for unauthenticated users
            RouterLink loginLink = new RouterLink("Login", com.xenplan.app.ui.view.publicview.LoginView.class);
            loginLink.getStyle().set("text-decoration", "none");
            menu.add(loginLink);
            
            RouterLink registerLink = new RouterLink("Register", com.xenplan.app.ui.view.publicview.RegisterView.class);
            registerLink.getStyle().set("text-decoration", "none");
            menu.add(registerLink);
        }

        add(title, menu);
        expand(title);
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private void logout() {
        VaadinServletRequest.getCurrent().getHttpServletRequest()
                .getSession().invalidate();
        SecurityContextHolder.clearContext();
        getUI().ifPresent(ui -> ui.navigate("login"));
    }
}
