package Coco.Products.backend;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    // ==============================
    // PASSWORD ENCODER
    // ==============================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==============================
    // SECURITY CONFIGURATION
    // ==============================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // Enable CORS
            .cors(cors -> {})

            // Disabled for local development.
            // We will configure CSRF properly before production.
            .csrf(csrf -> csrf.disable())

            // Allow our REST APIs
            .authorizeHttpRequests(auth -> auth

                // Allow CORS preflight requests
                .requestMatchers(
                        HttpMethod.OPTIONS,
                        "/**"
                ).permitAll()

                // Allow all APIs for now.
                // Controllers themselves check login/session
                // where authentication is required.
                .anyRequest().permitAll()
            )

            // We are using our own JSON login API
            .formLogin(form -> form.disable())

            // We are NOT using HTTP Basic authentication
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    // ==============================
    // CORS + SESSION COOKIE SUPPORT
    // ==============================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // React frontend
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // HTTP methods
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // Request headers
        configuration.setAllowedHeaders(
                List.of("*")
        );

        // IMPORTANT:
        // Allows browser to send JSESSIONID
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}