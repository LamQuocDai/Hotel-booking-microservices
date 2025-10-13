package com.hotelbooking.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test") // Không chạy khi profile là test
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - không cần authentication
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/register").permitAll()

                // Swagger endpoints - cho phép truy cập để test API
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()

                // H2 console cho test
                .requestMatchers("/h2-console/**").permitAll()

                // Tất cả các request khác cần authentication
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable()); // Cho phép H2 console
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
