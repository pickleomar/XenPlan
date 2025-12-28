package com.xenplan.app.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.vaadin.flow.spring.security.VaadinWebSecurityConfigurerAdapter;
import com.xenplan.app.ui.view.publicview.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // Public Vaadin routes
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/images/**",
                                "/VAADIN/**"
                        ).permitAll()

                        // Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Organizer
                        .requestMatchers("/organizer/**")
                        .hasAnyRole("ORGANIZER", "ADMIN")

                        // Client
                        .requestMatchers("/client/**")
                        .hasAnyRole("CLIENT", "ORGANIZER", "ADMIN")

                        // Everything else requires auth
                        .anyRequest().authenticated()
                );

        // Vaadin-specific security
        super.configure(http);

        // Custom login view
        setLoginView(http, LoginView.class);
    }
}
