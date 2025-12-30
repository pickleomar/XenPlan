package com.xenplan.app.ui.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

public class ConfirmDialog extends Dialog {

    private final Button confirmButton = new Button("Confirm");
    private final Button cancelButton = new Button("Cancel");
    private Consumer<Boolean> onConfirm;

    public ConfirmDialog(String title, String message) {
        setWidth("400px");
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        
        H3 titleComponent = new H3(title);
        titleComponent.getStyle().set("margin-top", "0");
        
        Paragraph messageComponent = new Paragraph(message);
        messageComponent.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            if (onConfirm != null) {
                onConfirm.accept(true);
            }
            close();
        });
        
        cancelButton.addClickListener(e -> {
            if (onConfirm != null) {
                onConfirm.accept(false);
            }
            close();
        });
        
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        
        VerticalLayout content = new VerticalLayout(titleComponent, messageComponent, buttonLayout);
        content.setSpacing(true);
        content.setPadding(false);
        
        add(content);
    }

    public void setOnConfirm(Consumer<Boolean> callback) {
        this.onConfirm = callback;
    }
}
