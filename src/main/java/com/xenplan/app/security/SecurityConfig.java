package com.xenplan.app.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.xenplan.app.ui.view.publicview.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers(
                        "/login",
                        "/images/**",
                        "/VAADIN/**",
                        "/h2-console/**"
                ).permitAll()

                // Role-based views
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/client/**").hasAnyRole("CLIENT", "ORGANIZER", "ADMIN")

        );

        // Let Vaadin finish security config (adds anyRequest internally)
        super.configure(http);

        // Custom Vaadin login view
        setLoginView(http, LoginView.class);

        // Needed for H2 console
        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
    }
}
