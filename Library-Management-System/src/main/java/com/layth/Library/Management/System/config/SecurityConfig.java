package com.layth.Library.Management.System.config;

import com.layth.Library.Management.System.utils.jwt.JwtRequestFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security: every request carries its own Bearer token, which JwtRequestFilter
 * turns into the Authentication for that request. Per-endpoint permissions are checked by
 * RoleCheckAspect (@RequireRole).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final SecurityProblemHandler securityProblemHandler;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, SecurityProblemHandler securityProblemHandler) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.securityProblemHandler = securityProblemHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF attacks ride on cookies the browser sends automatically. This API uses no
                // cookies or sessions, only an Authorization header, so CSRF protection is off.
                .csrf(AbstractHttpConfigurer::disable)
                // Never create or use an HttpSession (no JSESSIONID cookie, no saved requests).
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // Servlet errors forwarded to Spring Boot's /error page (for example a 400 from the
                        // request firewall) keep their real status instead of being masked by a 401.
                        // Only the internal ERROR dispatch is open; a client calling /error directly is not.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated()
                )
                // 401 for a missing or rejected token, 403 for a URL-level denial; both as problem details.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityProblemHandler)
                        .accessDeniedHandler(securityProblemHandler))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
