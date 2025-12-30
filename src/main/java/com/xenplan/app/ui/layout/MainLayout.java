package com.xenplan.app.ui.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {
        setPrimarySection(Section.NAVBAR);
        addToNavbar(new NavBar());
    }
}
