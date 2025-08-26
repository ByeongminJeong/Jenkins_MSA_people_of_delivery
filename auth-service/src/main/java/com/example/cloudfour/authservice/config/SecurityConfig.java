package com.example.cloudfour.authservice.config;

import com.example.cloudfour.modulecommon.filter.JwtClaimsAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    JwtClaimsAuthFilter jwtClaimsAuthFilter() {
        return new JwtClaimsAuthFilter();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login", "/auth/register/**", "/auth/refresh",
                                "/auth/email/**", "/.well-known/jwks.json",
                                "/v3/api-docs/**", "/swagger-ui/**", "/actuator/**","/users/actuator/**", "/store/actuator/**", "/module-common/actuator/**", "/carts/actuator/**", "/auth/actuator/**", "/payments/actuator/**", "/ai/actuator/**"
                        ).permitAll()
                        .requestMatchers("/auth/password", "/auth/email/change/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtClaimsAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
