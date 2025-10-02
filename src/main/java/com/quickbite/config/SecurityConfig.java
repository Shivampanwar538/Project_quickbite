package com.quickbite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF: Essential for MockMvc testing and common for stateless APIs.
                .csrf(AbstractHttpConfigurer::disable)

                // Define access rules for endpoints
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ACCESS (Pages and Static Resources)
                        .requestMatchers("/**","/menu.html", "/admin.html", "/orders.html", "/index.html", "/login.html", "/register.html", "/css/**", "/js/**","/favicon.ico").permitAll()

                        // 2. PUBLIC API ENDPOINTS (Authentication & Menu Viewing)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/menu").permitAll()

                        // 3. SECURED USER ENDPOINTS (Authenticated required for these specific paths)
                        .requestMatchers(HttpMethod.POST, "/order/place").authenticated()
                        .requestMatchers(HttpMethod.GET, "/order/user/*").authenticated()


                        // 4. SECURED ADMIN ENDPOINTS
                        // Grouping all ADMIN paths based on method/pattern for clarity and compilation:
                        .requestMatchers("/admin/**", "/order/all", "/order/pending").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/order/*/status").hasRole("ADMIN")

                        // Menu Management (POST/PUT/DELETE /menu)
                        .requestMatchers(HttpMethod.POST, "/menu").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/menu/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/menu/*").hasRole("ADMIN")

                        // 5. DEFAULT FALLBACK: All other requests must be authenticated
                        .anyRequest().authenticated()
                );


        return http.build();
    }
}