package com.insurecloud.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

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
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("realm_access.roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return jwtAuthenticationConverter;
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
