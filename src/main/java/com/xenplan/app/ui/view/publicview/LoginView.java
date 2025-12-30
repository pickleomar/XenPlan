package com.xenplan.app.ui.view.publicview;

import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route("login")
@PageTitle("Login | XenPlan")
public class LoginView extends LoginOverlay {

    public LoginView() {
        setTitle("XenPlan");
        setDescription("Secure Event Management");
        setAction("login"); // IMPORTANT: Spring Security endpoint
        setOpened(true);
        setForgotPasswordButtonVisible(false);
    }
}
