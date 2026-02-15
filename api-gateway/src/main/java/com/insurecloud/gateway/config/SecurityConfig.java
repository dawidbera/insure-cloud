package com.insurecloud.gateway.config;

import com.insurecloud.common.security.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration for API Gateway security.
 * Sets up the gateway as an OAuth2 Resource Server.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain for the gateway.
     * 
     * @param http The ServerHttpSecurity object.
     * @return The configured SecurityWebFilterChain.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/eureka/**", "/actuator/**").permitAll()
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                .pathMatchers("/v3/api-docs/policy-service", "/v3/api-docs/quote-service", "/v3/api-docs/search-service", "/v3/api-docs/document-service").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                jwt -> jwt.jwtAuthenticationConverter(KeycloakJwtAuthenticationConverter.createReactiveConverter())
            ));
        return http.build();
    }
}
