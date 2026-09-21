package project.secure_api_hardening.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // v1-auth-hardened: /admin/users now requires the ADMIN role from a valid
    // Keycloak-issued JWT; everything else just requires authentication.
    // Replaces the v0-vulnerable permitAll() baseline.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/users").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                // Runs before Keycloak/JWT auth checks, so abusive traffic is
                // rejected as cheaply as possible, before any auth work happens.
                .addFilterBefore(new RateLimitFilter(), BearerTokenAuthenticationFilter.class)
                // X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, and
                // cache-control headers are already added by Spring Security's
                // defaults. CSP and HSTS are opt-in, so they're configured here.
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        // Only sent on HTTPS requests (browsers ignore it over plain
                        // HTTP anyway) — this demo runs over HTTP locally, so it won't
                        // appear until the app is actually deployed behind TLS.
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)));
        return http.build();
    }
}