package com.xenplan.app.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.service.UserService;
import com.xenplan.app.ui.layout.MainLayout;
import com.xenplan.app.security.SecurityUtils;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("My Profile | XenPlan")
@RolesAllowed({"CLIENT", "ORGANIZER", "ADMIN"})
public class UserProfileView extends VerticalLayout {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final User currentUser;
    private final BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
    
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final EmailField emailField = new EmailField("Email");
    private final TextField phoneField = new TextField("Phone");
    
    private final Button saveButton = new Button("Save Changes");
    private final Button changePasswordButton = new Button("Change Password");
    
    private Div roleBadge;
    private Div statusBadge;

    public UserProfileView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = SecurityUtils.getCurrentUser();
        
        if (currentUser == null) {
            return;
        }
        
        // --- 1. CENTER THE MAIN VIEW ---
        setSizeFull(); 
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER); // Center horizontally
        setJustifyContentMode(JustifyContentMode.CENTER); // Center vertically
        
        setupView();
        loadUserData();
    }

    private void setupView() {
        // --- 2. CONTAINER FOR CONTENT ---
        // This holds all elements together within a max-width
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setMaxWidth("800px");
        contentContainer.setWidthFull();
        contentContainer.setSpacing(true);
        contentContainer.setPadding(false);
        contentContainer.setAlignItems(Alignment.CENTER); // Center items inside the card
        
        H2 title = new H2("My Profile");
        title.getStyle().set("margin-top", "0");
        
        // Account metadata badges
        HorizontalLayout badgesLayout = new HorizontalLayout();
        badgesLayout.setSpacing(true);
        badgesLayout.setAlignItems(Alignment.CENTER);
        
        roleBadge = createBadge("Role: " + currentUser.getRole().name(), 
                getRoleBadgeColor(currentUser.getRole()));
        statusBadge = createBadge(
                Boolean.TRUE.equals(currentUser.getActive()) ? "Active" : "Inactive",
                Boolean.TRUE.equals(currentUser.getActive()) 
                        ? "var(--lumo-success-color)" 
                        : "var(--lumo-error-color)");
        
        badgesLayout.add(roleBadge, statusBadge);
        
        Paragraph subtitle = new Paragraph("Manage your account information and security settings");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("margin-top", "0");
        subtitle.getStyle().set("margin-bottom", "1rem");
        subtitle.getStyle().set("text-align", "center"); // Center text
        
        // Profile form
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setWidthFull();
        
        // Configure fields
        firstNameField.setRequired(true);
        firstNameField.setMaxLength(50);
        firstNameField.setWidthFull();
        
        lastNameField.setRequired(true);
        lastNameField.setMaxLength(50);
        lastNameField.setWidthFull();
        
        emailField.setRequired(true);
        emailField.setReadOnly(true); 
        emailField.setWidthFull();
        emailField.setHelperText("Email cannot be changed");
        
        phoneField.setMaxLength(20);
        phoneField.setWidthFull();
        
        formLayout.add(firstNameField, 1);
        formLayout.add(lastNameField, 1);
        formLayout.add(emailField, 2);
        formLayout.add(phoneField, 1);
        
        // Configure binder
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(User::getFirstName, User::setFirstName);
        
        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(User::getLastName, User::setLastName);
        
        binder.forField(phoneField)
                .bind(User::getPhone, User::setPhone);
        
        // Buttons
        saveButton.setText("Save Changes");
        saveButton.setIcon(new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1)");
        saveButton.addClickListener(e -> handleSave());
        
        changePasswordButton.setIcon(new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.LOCK));
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        changePasswordButton.getStyle().set("box-shadow", "0 4px 6px -1px rgba(0, 0, 0, 0.1)");
        changePasswordButton.addClickListener(e -> openChangePasswordDialog());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, changePasswordButton);
        buttonLayout.setWidthFull();
        // Center the buttons to match the rest of the layout
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER); 
        buttonLayout.setSpacing(true);
        
        // --- 3. ADD EVERYTHING TO CONTAINER, THEN CONTAINER TO VIEW ---
        contentContainer.add(title, badgesLayout, subtitle, formLayout, buttonLayout);
        add(contentContainer);
    }

    private void loadUserData() {
        // Reload user to get latest data
        User user = userService.findById(currentUser.getId())
                .orElse(currentUser);
        
        binder.readBean(user);
    }

    private void handleSave() {
        User userData = new User();
        
        try {
            binder.writeBean(userData);
        } catch (ValidationException e) {
            Notification.show("Please fix the validation errors", 3000, Notification.Position.MIDDLE);
            return;
        }
        
        // Preserve email and other fields that shouldn't change
        userData.setEmail(currentUser.getEmail());
        userData.setRole(currentUser.getRole());
        userData.setActive(currentUser.getActive());
        
        saveButton.setEnabled(false);
        
        try {
            User updatedUser = userService.updateUser(currentUser.getId(), userData, currentUser);
            Notification.show("Profile updated successfully", 5000, Notification.Position.MIDDLE);
            
            // Update badges if status changed
            if (updatedUser.getActive() != null) {
                statusBadge.setText(Boolean.TRUE.equals(updatedUser.getActive()) ? "Active" : "Inactive");
                if (Boolean.TRUE.equals(updatedUser.getActive())) {
                    statusBadge.getStyle().set("background", "var(--lumo-success-color-10pct)");
                    statusBadge.getStyle().set("color", "var(--lumo-success-color)");
                } else {
                    statusBadge.getStyle().set("background", "var(--lumo-error-color-10pct)");
                    statusBadge.getStyle().set("color", "var(--lumo-error-color)");
                }
            }
            
        } catch (ConflictException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (BusinessException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
        } finally {
            saveButton.setEnabled(true);
        }
    }

    private void openChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setModal(true);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        
        H3 dialogTitle = new H3("Change Password");
        dialogTitle.getStyle().set("margin-top", "0");
        
        PasswordField currentPasswordField = new PasswordField("Current Password");
        currentPasswordField.setRequired(true);
        currentPasswordField.setWidthFull();
        
        PasswordField newPasswordField = new PasswordField("New Password");
        newPasswordField.setRequired(true);
        newPasswordField.setMinLength(8);
        newPasswordField.setWidthFull();
        newPasswordField.setHelperText("Password must be at least 8 characters");
        
        PasswordField confirmPasswordField = new PasswordField("Confirm New Password");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidthFull();
        
        confirmPasswordField.addValueChangeListener(e -> {
            if (!e.getValue().equals(newPasswordField.getValue())) {
                confirmPasswordField.setErrorMessage("Passwords do not match");
                confirmPasswordField.setInvalid(true);
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });
        
        newPasswordField.addValueChangeListener(e -> {
            if (confirmPasswordField.getValue() != null && 
                !confirmPasswordField.getValue().equals(e.getValue())) {
                confirmPasswordField.setErrorMessage("Passwords do not match");
                confirmPasswordField.setInvalid(true);
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });
        
        Button confirmButton = new Button("Change Password");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(e -> {
            String currentPassword = currentPasswordField.getValue();
            String newPassword = newPasswordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();
            
            if (currentPassword == null || currentPassword.isEmpty()) {
                Notification.show("Current password is required", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            if (newPassword == null || newPassword.length() < 8) {
                Notification.show("New password must be at least 8 characters", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Notification.show("New passwords do not match", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            try {
                userService.changePassword(currentUser.getId(), currentPassword, newPassword, currentUser);
                Notification.show("Password changed successfully", 5000, Notification.Position.MIDDLE);
                dialog.close();
            } catch (ConflictException ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            } catch (BusinessException ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
            }
        });
        
        Button cancelButton = new Button("Cancel", new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> dialog.close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        
        VerticalLayout dialogContent = new VerticalLayout(
                dialogTitle,
                currentPasswordField,
                newPasswordField,
                confirmPasswordField,
                buttonLayout
        );
        dialogContent.setSpacing(true);
        dialogContent.setPadding(false);
        
        dialog.add(dialogContent);
        dialog.open();
    }

    private Div createBadge(String text, String color) {
        Div badge = new Div();
        badge.setText(text);
        badge.getStyle().set("display", "inline-block");
        if (color.contains("success")) {
            badge.getStyle().set("background", "var(--lumo-success-color-10pct)");
        } else if (color.contains("error")) {
            badge.getStyle().set("background", "var(--lumo-error-color-10pct)");
        } else if (color.contains("primary")) {
            badge.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        } else {
            badge.getStyle().set("background", "var(--lumo-contrast-10pct)");
        }
        badge.getStyle().set("color", color);
        badge.getStyle().set("padding", "0.5rem 1rem");
        badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        badge.getStyle().set("font-weight", "500");
        badge.getStyle().set("font-size", "var(--lumo-font-size-s)");
        return badge;
    }

    private String getRoleBadgeColor(Role role) {
        switch (role) {
            case ADMIN:
                return "var(--lumo-error-color)";
            case ORGANIZER:
                return "var(--lumo-primary-color)";
            case CLIENT:
            default:
                return "var(--lumo-success-color)";
        }
    }
}