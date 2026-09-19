package project.secure_api_hardening.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // v0-vulnerable baseline: permits every request, overriding Spring Boot's
    // default auto-login-for-everything behavior, so /admin/users is genuinely
    // open (the intended vulnerability) instead of accidentally protected.
    // Replaced with OAuth2/role-based rules in a later hardening step.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }
}