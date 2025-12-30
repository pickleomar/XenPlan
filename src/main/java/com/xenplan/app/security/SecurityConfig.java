package com.xenplan.app.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.xenplan.app.ui.view.publicview.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers(
                        "/login",
                        "/register",
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

        // Custom success handler for role-based redirection
        http.formLogin(form -> form.successHandler(authenticationSuccessHandler()));

        // Needed for H2 console
        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            
            String redirectUrl;
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
            } else if (role.equals("ROLE_ORGANIZER")) {
                redirectUrl = "/organizer/dashboard";
            } else if (role.equals("ROLE_CLIENT")) {
                redirectUrl = "/client/dashboard";
            } else {
                redirectUrl = "/";
            }
            
            response.sendRedirect(redirectUrl);
        };
    }
}
