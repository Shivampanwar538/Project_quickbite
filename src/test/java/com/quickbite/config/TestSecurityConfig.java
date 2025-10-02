package com.quickbite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for testing
                .csrf(AbstractHttpConfigurer::disable)
                
                // Disable session management for testing
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )

                // Define access rules for endpoints - match production security rules
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ACCESS (Pages and Static Resources)
                        .requestMatchers("/menu.html", "/admin.html", "/orders.html", "/index.html", "/login.html", "/register.html", "/css/**", "/js/**", "/favicon.ico").permitAll()

                        // 2. PUBLIC API ENDPOINTS (Authentication & Menu Viewing)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/menu").permitAll()

                        // 3. SECURED USER ENDPOINTS (Authenticated required for these specific paths)
                        .requestMatchers(HttpMethod.POST, "/order/place").authenticated()
                        .requestMatchers(HttpMethod.GET, "/order/user/*").authenticated()

                        // 4. SECURED ADMIN ENDPOINTS
                        .requestMatchers("/order/all", "/order/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/order/*/status").hasRole("ADMIN")

                        // Menu Management (POST/PUT/DELETE /menu)
                        .requestMatchers(HttpMethod.POST, "/menu").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/menu/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/menu/*").hasRole("ADMIN")

                        // 5. DEFAULT FALLBACK: All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                
                // Disable form login and logout for testing
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
