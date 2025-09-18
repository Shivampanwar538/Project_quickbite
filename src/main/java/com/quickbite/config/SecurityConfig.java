package com.quickbite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())              // ❌ Disable CSRF (safe for dev only)
                .headers(headers -> headers.frameOptions().disable()) // Allow H2 frames
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()             // ✅ Allow all endpoints
                )
                .formLogin(form -> form.disable())        // Disable login form
                .httpBasic(basic -> basic.disable());     // Disable HTTP Basic

        return http.build();
    }
}
