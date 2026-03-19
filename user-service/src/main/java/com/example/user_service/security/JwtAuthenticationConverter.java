package com.example.user_service.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Keycloak met les rôles dans le claim "realm_access.roles"
 * Ce converter les transforme en objets Spring Security (ROLE_USER, ROLE_ADMIN...)
 */
@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        // Lire le claim "realm_access" du token JWT
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                roles.stream()
                     // Transformer "ADMIN" → "ROLE_ADMIN" (convention Spring)
                     .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                     .forEach(authorities::add);
            }
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
