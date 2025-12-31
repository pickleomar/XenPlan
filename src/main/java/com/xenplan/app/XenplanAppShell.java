package com.xenplan.app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

// UNDO: Removed value="xenplan". This tells Vaadin to use the built-in Lumo theme.
@Theme(variant = Lumo.DARK)
public class XenplanAppShell implements AppShellConfigurator {
}