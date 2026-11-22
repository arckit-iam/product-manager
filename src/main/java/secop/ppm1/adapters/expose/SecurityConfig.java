// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Protège l'API par jeton JWT émis par arc-auth. Les rôles ARC proviennent du claim
 * {@code roles}. L'API est sans état : pas de session, donc pas de protection CSRF.
 *
 * <ul>
 *   <li>{@code GET /products/**}, {@code /accounts/**} et {@code /account-types} : {@code ARC_READER}, {@code ARC_PUBLISHER} ou {@code ARC_ADMIN}</li>
 *   <li>{@code POST}, {@code PUT} et {@code DELETE} sur ces chemins : {@code ARC_PUBLISHER} ou {@code ARC_ADMIN}</li>
 *   <li>{@code /actuator/health} : public (sondes)</li>
 *   <li>tout le reste (dont les dépôts exposés par Spring Data REST) : refusé</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ARC_ADMIN";
    private static final String PUBLISHER = "ARC_PUBLISHER";
    private static final String READER = "ARC_READER";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**", "/accounts", "/accounts/**",
                                "/account-types")
                                .hasAnyRole(READER, PUBLISHER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/products", "/products/**", "/accounts", "/accounts/**")
                                .hasAnyRole(PUBLISHER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/products", "/products/**", "/accounts/**")
                                .hasAnyRole(PUBLISHER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/products", "/products/**", "/accounts/**")
                                .hasAnyRole(PUBLISHER, ADMIN)
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /** Transforme le claim {@code roles} en autorités {@code ROLE_ARC_*}. */
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
