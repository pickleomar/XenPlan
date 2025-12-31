package com.xenplan.app.ui.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout implements RouterLayout {

    public MainLayout() {
        setPrimarySection(Section.NAVBAR);
        addToNavbar(new NavBar());
        
        // Force Dark Mode by default
        UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
    }
}
