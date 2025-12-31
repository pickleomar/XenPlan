package com.xenplan.app.ui.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.Lumo;
import com.xenplan.app.security.SecurityUtils;
import com.xenplan.app.ui.view.UserProfileView;
import com.xenplan.app.ui.view.admin.AdminDashboardView;
import com.xenplan.app.ui.view.client.ClientDashboardView;
import com.xenplan.app.ui.view.organizer.OrganizerDashboardView;
import com.xenplan.app.ui.view.publicview.EventListView;
import com.xenplan.app.ui.view.publicview.LoginView;
import com.xenplan.app.ui.view.publicview.RegisterView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class NavBar extends HorizontalLayout {

    public NavBar() {
        // 1. CONTAINER STYLING
        setWidthFull();
        setHeight("70px");
        setPadding(true);
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        // "Glass" Effect Background
        getStyle().set("background-color", "rgba(44, 47, 87, 0.9)"); 
        getStyle().set("backdrop-filter", "blur(10px)");
        getStyle().set("border-bottom", "1px solid rgba(255, 255, 255, 0.1)");
        getStyle().set("position", "sticky");
        getStyle().set("top", "0");
        getStyle().set("z-index", "100");

        // --- LEFT: LOGO ---
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(Alignment.CENTER);
        
        Image logo = new Image("images/xenplan-logo.png", "XenPlan");
        logo.setHeight("40px");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        
        logoLayout.add(logo);

        // --- CENTER: NAVIGATION LINKS ---
        HorizontalLayout navLinks = new HorizontalLayout();
        navLinks.setSpacing(true);
        navLinks.setAlignItems(Alignment.CENTER);
        
        navLinks.add(createNavLink("Events", VaadinIcon.CALENDAR, EventListView.class));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                !auth.getName().equals("anonymousUser");

        if (isAuthenticated) {
            if (SecurityUtils.hasRole("CLIENT")) {
                navLinks.add(createNavLink("Dashboard", VaadinIcon.DASHBOARD, ClientDashboardView.class));
            }
            if (SecurityUtils.hasRole("ORGANIZER")) {
                navLinks.add(createNavLink("Organizer", VaadinIcon.BRIEFCASE, OrganizerDashboardView.class));
            }
            if (SecurityUtils.hasRole("ADMIN")) {
                // FIXED: VaadinIcon.TOOLS (was tools)
                navLinks.add(createNavLink("Admin", VaadinIcon.TOOLS, AdminDashboardView.class));
            }
        }

        // --- RIGHT: USER ACTIONS ---
        HorizontalLayout rightActions = new HorizontalLayout();
        rightActions.setSpacing(true);
        rightActions.setAlignItems(Alignment.CENTER);

        Button themeToggle = new Button();
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        themeToggle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getElement().getThemeList().contains(Lumo.DARK)) {
             themeToggle.setIcon(new Icon(VaadinIcon.MOON));
        } else {
             themeToggle.setIcon(new Icon(VaadinIcon.SUN_O));
        }

        themeToggle.addClickListener(e -> {
            if (currentUI != null) {
                com.vaadin.flow.dom.ThemeList themeList = currentUI.getElement().getThemeList();
                if (themeList.contains(Lumo.DARK)) {
                    themeList.remove(Lumo.DARK);
                    themeToggle.setIcon(new Icon(VaadinIcon.SUN_O));
                } else {
                    themeList.add(Lumo.DARK);
                    themeToggle.setIcon(new Icon(VaadinIcon.MOON));
                }
            }
        });
        rightActions.add(themeToggle);

        if (isAuthenticated) {
            Button profileBtn = new Button(new Icon(VaadinIcon.USER));
            profileBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            profileBtn.setTooltipText("My Profile");
            profileBtn.getStyle().set("color", "var(--lumo-primary-color)");
            
            RouterLink profileLink = new RouterLink("", UserProfileView.class);
            profileLink.add(profileBtn);
            
            Button logoutBtn = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT));
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logoutBtn.getStyle().set("color", "var(--lumo-error-color)");
            logoutBtn.getStyle().set("font-size", "var(--lumo-font-size-s)");
            logoutBtn.addClickListener(e -> logout());

            rightActions.add(profileLink, logoutBtn);
        } else {
            RouterLink loginLink = new RouterLink("Login", LoginView.class);
            styleAuthLink(loginLink);
            
            Button registerBtn = new Button("Sign Up");
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(RegisterView.class)));

            rightActions.add(loginLink, registerBtn);
        }

        add(logoLayout, navLinks, rightActions);
    }

    private RouterLink createNavLink(String text, VaadinIcon icon, Class<? extends com.vaadin.flow.component.Component> view) {
        RouterLink link = new RouterLink();
        link.setRoute(view);
        
        Span textSpan = new Span(text);
        Icon iconSpan = new Icon(icon);
        iconSpan.setSize("16px");
        
        HorizontalLayout content = new HorizontalLayout(iconSpan, textSpan);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        
        link.add(content);
        
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--lumo-secondary-text-color)");
        link.getStyle().set("font-weight", "500");
        link.getStyle().set("font-size", "0.95rem");
        link.getStyle().set("padding", "0.5rem 1rem");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        link.getStyle().set("transition", "all 0.2s");
        
        return link;
    }
    
    private void styleAuthLink(RouterLink link) {
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--lumo-body-text-color)");
        link.getStyle().set("font-weight", "600");
        link.getStyle().set("margin-right", "1rem");
    }

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }
}