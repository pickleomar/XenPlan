package com.xenplan.app.ui.layout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletRequest;
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

        H1 title = new H1("XenPlan");
        title.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        title.getStyle().set("cursor", "pointer");

        HorizontalLayout menu = new HorizontalLayout();
        menu.setSpacing(true);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                !auth.getName().equals("anonymousUser");

        // Public link
        menu.add(new RouterLink("Events", EventListView.class));

        if (isAuthenticated) {
            // Role-based links
            if (hasRole("CLIENT")) {
                menu.add(new RouterLink("Dashboard", ClientDashboardView.class));
            }

            if (hasRole("ORGANIZER")) {
                menu.add(new RouterLink("Organizer", OrganizerDashboardView.class));
            }

            if (hasRole("ADMIN")) {
                menu.add(new RouterLink("Admin", AdminDashboardView.class));
            }

            Button logout = new Button("Logout", e -> logout());
            menu.add(logout);
        } else {
            // Show login/register for unauthenticated users
            menu.add(new RouterLink("Login", com.xenplan.app.ui.view.publicview.LoginView.class));
            menu.add(new RouterLink("Register", com.xenplan.app.ui.view.publicview.RegisterView.class));
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
