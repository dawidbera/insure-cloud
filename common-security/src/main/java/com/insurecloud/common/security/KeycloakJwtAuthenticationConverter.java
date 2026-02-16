package com.insurecloud.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared converter to extract roles from Keycloak JWT and prefix them with ROLE_.
 */
public class KeycloakJwtAuthenticationConverter {

    /**
     * Creates and configures a JwtAuthenticationConverter for Keycloak (Servlet-based).
     * 
     * @return A configured JwtAuthenticationConverter.
     */
    public static JwtAuthenticationConverter createConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(KeycloakJwtAuthenticationConverter::extractAuthorities);
        return jwtAuthenticationConverter;
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * Creates and configures a Reactive JwtAuthenticationConverter for Keycloak (WebFlux-based).
     * 
     * @return A configured reactive converter.
     */
    public static Converter<Jwt, Mono<AbstractAuthenticationToken>> createReactiveConverter() {
        return new ReactiveJwtAuthenticationConverterAdapter(createConverter());
    }
}
