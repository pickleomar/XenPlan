package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.exception.BusinessException;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.service.UserService;
import com.xenplan.app.security.SecurityUtils;

@Route("register")
@PageTitle("Register | XenPlan")
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
    
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmPasswordField = new PasswordField("Confirm Password");
    private final Button submitButton = new Button("Register");
    private final Button cancelButton = new Button("Cancel");

    public RegisterView(UserService userService) {
        this.userService = userService;
        
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        
        setupForm();
    }

    private void setupForm() {
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setWidth("400px");
        formContainer.setPadding(false);
        formContainer.setSpacing(false);
        
        H2 title = new H2("Create Account");
        title.getStyle().set("margin-bottom", "1rem");
        
        Paragraph description = new Paragraph("Join XenPlan to reserve events and manage your bookings.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        description.getStyle().set("margin-bottom", "2rem");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );
        formLayout.setWidthFull();
        
        // Configure fields
        firstNameField.setRequired(true);
        firstNameField.setWidthFull();
        firstNameField.setMaxLength(50);
        
        lastNameField.setRequired(true);
        lastNameField.setWidthFull();
        lastNameField.setMaxLength(50);
        
        emailField.setRequired(true);
        emailField.setWidthFull();
        emailField.setMaxLength(150);
        
        passwordField.setRequired(true);
        passwordField.setWidthFull();
        passwordField.setMinLength(8);
        passwordField.setHelperText("Minimum 8 characters");
        
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setHelperText("Re-enter your password");
        
        formLayout.add(firstNameField, 1);
        formLayout.add(lastNameField, 1);
        formLayout.add(emailField, 1);
        formLayout.add(passwordField, 1);
        formLayout.add(confirmPasswordField, 1);
        
        // Configure binder
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(User::getFirstName, User::setFirstName);
        
        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(User::getLastName, User::setLastName);
        
        binder.forField(emailField)
                .asRequired("Email is required")
                .withValidator(email -> email != null && !email.isEmpty(), "Email is required")
                .withValidator(email -> email == null || email.matches("^[A-Za-z0-9+_.-]+@(.+)$"), "Invalid email format")
                .bind(User::getEmail, User::setEmail);
        
        binder.forField(passwordField)
                .asRequired("Password is required")
                .withValidator(pwd -> pwd != null && pwd.length() >= 8, "Password must be at least 8 characters")
                .bind(user -> "", (user, pwd) -> {}); // Don't bind to entity, handle separately
        
        // Custom validation for password confirmation
        confirmPasswordField.addValueChangeListener(e -> {
            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                confirmPasswordField.setInvalid(true);
                confirmPasswordField.setErrorMessage("Passwords do not match");
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });
        
        passwordField.addValueChangeListener(e -> {
            if (!passwordField.getValue().equals(confirmPasswordField.getValue()) && 
                !confirmPasswordField.getValue().isEmpty()) {
                confirmPasswordField.setInvalid(true);
                confirmPasswordField.setErrorMessage("Passwords do not match");
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });
        
        // Buttons
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();
        submitButton.addClickListener(e -> handleRegistration());
        
        cancelButton.setWidthFull();
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));
        
        VerticalLayout buttonLayout = new VerticalLayout(submitButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        
        formContainer.add(title, description, formLayout, buttonLayout);
        add(formContainer);
    }

    private void handleRegistration() {
        // Validate password confirmation
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification.show("Passwords do not match", 3000, Notification.Position.MIDDLE);
            confirmPasswordField.setInvalid(true);
            return;
        }
        
        // Validate binder
        User user = new User();
        try {
            binder.writeBean(user);
        } catch (ValidationException e) {
            Notification.show("Please fix the validation errors", 3000, Notification.Position.MIDDLE);
            return;
        }
        
        // Disable submit button during processing
        submitButton.setEnabled(false);
        
        try {
            // Register user
            userService.registerUser(
                    firstNameField.getValue(),
                    lastNameField.getValue(),
                    emailField.getValue(),
                    passwordField.getValue()
            );
            
            Notification.show("Registration successful! Please login.", 5000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("login"));
            
        } catch (ConflictException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
            emailField.setInvalid(true);
            emailField.setErrorMessage(e.getMessage());
        } catch (BusinessException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("An error occurred. Please try again.", 5000, Notification.Position.MIDDLE);
        } finally {
            submitButton.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Redirect if already authenticated
        if (SecurityUtils.isAuthenticated()) {
            com.xenplan.app.domain.entity.User user = SecurityUtils.getCurrentUser();
            if (user != null) {
                String redirectUrl;
                switch (user.getRole()) {
                    case ADMIN:
                        redirectUrl = "/admin/dashboard";
                        break;
                    case ORGANIZER:
                        redirectUrl = "/organizer/dashboard";
                        break;
                    case CLIENT:
                        redirectUrl = "/client/dashboard";
                        break;
                    default:
                        redirectUrl = "/";
                }
                event.forwardTo(redirectUrl);
            }
        }
    }
}

