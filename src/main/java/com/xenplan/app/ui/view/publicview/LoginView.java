package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.xenplan.app.security.SecurityUtils;

@Route("login")
@PageTitle("Login | XenPlan")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        configureLoginForm();
        add(loginForm);
    }

    private void configureLoginForm() {
        loginForm.setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("XenPlan");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Login");
        i18n.getForm().setForgotPassword("Register");

        i18n.getErrorMessage().setTitle("Invalid credentials");
        i18n.getErrorMessage().setMessage(
                "Check your email and password and try again."
        );

        loginForm.setI18n(i18n);

        loginForm.addForgotPasswordListener(e ->
                UI.getCurrent().navigate("register")
        );
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
                return;
            }
        }

        // Show error if login failed
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
