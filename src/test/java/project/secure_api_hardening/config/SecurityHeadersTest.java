package project.secure_api_hardening.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import project.secure_api_hardening.user.UserController;
import project.secure_api_hardening.user.UserRepository;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void includesContentSecurityPolicyOnEveryResponse() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'"));
    }

    @Test
    void includesStrictTransportSecurityOnlyOverHttps() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        // Plain HTTP: browsers ignore HSTS here anyway, so Spring Security omits it.
        mockMvc.perform(get("/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(header().doesNotExist("Strict-Transport-Security"));

        // Simulated HTTPS request: header should be present.
        mockMvc.perform(get("/admin/users")
                        .secure(true)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(header().string("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"));
    }
}