package com.example.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(withDefaults())
                )
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.GET, "/cashcards/**").hasAuthority("SCOPE_cashcard:read")
                        .requestMatchers("/cashcards/**").hasAuthority("SCOPE_cashcard:write")
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
