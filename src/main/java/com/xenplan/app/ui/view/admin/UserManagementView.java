package com.xenplan.app.ui.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.service.UserService;
import com.xenplan.app.ui.component.ConfirmDialog;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("User Management | XenPlan")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final User currentUser;
    private Grid<User> usersGrid;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    public UserManagementView(UserService userService) {
        this.userService = userService;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        
        setupHeader();
        setupGrid();
        loadUsers();
    }

    private void setupHeader() {
        H2 title = new H2("User Management");
        title.getStyle().set("margin-top", "0");
        
        RouterLink backLink = new RouterLink("← Back to Dashboard", AdminDashboardView.class);
        backLink.getStyle().set("text-decoration", "none");
        backLink.getStyle().set("margin-bottom", "1rem");
        
        add(title, backLink);
    }

    private void setupGrid() {
        usersGrid = new Grid<>(User.class, false);
        usersGrid.setWidthFull();
        usersGrid.setAllRowsVisible(true);
        
        usersGrid.addColumn(u -> u.getFirstName() + " " + u.getLastName())
                .setHeader("Name")
                .setSortable(true)
                .setAutoWidth(true);
        
        usersGrid.addColumn(User::getEmail)
                .setHeader("Email")
                .setSortable(true)
                .setAutoWidth(true);
        
        usersGrid.addColumn(u -> u.getRole().name())
                .setHeader("Role")
                .setSortable(true)
                .setAutoWidth(true);
        
        usersGrid.addColumn(u -> u.getRegistrationDate().format(DATE_FORMATTER))
                .setHeader("Registration Date")
                .setSortable(true)
                .setAutoWidth(true);
        
        usersGrid.addComponentColumn(user -> {
            Div div = new Div();
            div.setText(Boolean.TRUE.equals(user.getActive()) ? "Active" : "Inactive");
            div.getStyle().set("color", Boolean.TRUE.equals(user.getActive()) 
                    ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
            div.getStyle().set("font-weight", "500");
            return div;
        })
        .setHeader("Status")
        .setAutoWidth(true);
        
        usersGrid.addComponentColumn(user -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setSpacing(true);
            actionsLayout.setPadding(false);
            
            // Status toggle button
            Button statusButton = new Button();
            if (user.getId().equals(currentUser.getId())) {
                statusButton.setText("Current User");
                statusButton.setEnabled(false);
                actionsLayout.add(statusButton);
            } else {
                if (Boolean.TRUE.equals(user.getActive())) {
                    statusButton.setText("Deactivate");
                    statusButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                    statusButton.addClickListener(e -> handleToggleActive(user.getId(), false));
                } else {
                    statusButton.setText("Activate");
                    statusButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
                    statusButton.addClickListener(e -> handleToggleActive(user.getId(), true));
                }
                actionsLayout.add(statusButton);
                
                // Role change button (only for CLIENT and ORGANIZER, not for ADMIN or current user)
                if (user.getRole() != Role.ADMIN) {
                    Button roleButton = new Button();
                    if (user.getRole() == Role.CLIENT) {
                        roleButton.setText("Make Organizer");
                        roleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                        roleButton.addClickListener(e -> handleChangeRole(user.getId(), Role.ORGANIZER));
                    } else if (user.getRole() == Role.ORGANIZER) {
                        roleButton.setText("Make Client");
                        roleButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
                        roleButton.addClickListener(e -> handleChangeRole(user.getId(), Role.CLIENT));
                    }
                    actionsLayout.add(roleButton);
                }
            }
            
            return actionsLayout;
        })
        .setHeader("Actions")
        .setAutoWidth(true);
        
        add(usersGrid);
    }

    private void loadUsers() {
        List<User> users = userService.findAll();
        usersGrid.setItems(users);
        
        if (users.isEmpty()) {
            Paragraph noUsers = new Paragraph("No users found.");
            noUsers.getStyle().set("color", "var(--lumo-secondary-text-color)");
            noUsers.getStyle().set("text-align", "center");
            noUsers.getStyle().set("padding", "2rem");
            add(noUsers);
        }
    }

    private void handleToggleActive(UUID userId, boolean active) {
        String action = active ? "activate" : "deactivate";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Action",
                "Are you sure you want to " + action + " this user?"
        );
        
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    userService.setUserActive(userId, active, currentUser);
                    Notification.show("User " + action + "d successfully", 5000, Notification.Position.MIDDLE);
                    loadUsers();
                } catch (BusinessException e) {
                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
                } catch (Exception e) {
                    Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
                }
            }
        });
        
        dialog.open();
    }

    private void handleChangeRole(UUID userId, Role newRole) {
        String roleName = newRole == Role.ORGANIZER ? "Organizer" : "Client";
        ConfirmDialog dialog = new ConfirmDialog(
                "Change User Role",
                "Are you sure you want to change this user's role to " + roleName + "?"
        );
        
        dialog.setOnConfirm(confirmed -> {
            if (confirmed) {
                try {
                    userService.changeUserRole(userId, newRole, currentUser);
                    Notification.show("User role changed to " + roleName + " successfully", 5000, Notification.Position.MIDDLE);
                    loadUsers();
                } catch (BusinessException e) {
                    Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
                } catch (Exception e) {
                    Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
                }
            }
        });
        
        dialog.open();
    }
}

