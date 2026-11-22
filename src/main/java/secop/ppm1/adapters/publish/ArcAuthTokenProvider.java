// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Obtient auprès d'arc-auth le jeton du compte de service, et le conserve jusqu'à
 * peu avant son expiration. Un seul jeton est partagé par tous les appels.
 */
@Component
@ConditionalOnProperty(name = "arc.orders.channel", havingValue = "api")
public class ArcAuthTokenProvider {

    /** Le jeton est renouvelé un peu avant son expiration pour ne jamais en présenter un périmé. */
    static final Duration SAFETY_MARGIN = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final String username;
    private final String password;
    private final Clock clock;

    private String token;
    private Instant refreshAt = Instant.MIN;

    @Autowired
    public ArcAuthTokenProvider(
            @Value("${arc.auth.url}") String url,
            @Value("${arc.auth.username}") String username,
            @Value("${arc.auth.password}") String password) {
        this(RestClient.builder(), url, username, password, Clock.systemUTC());
    }

    ArcAuthTokenProvider(RestClient.Builder builder, String url, String username, String password, Clock clock) {
        this.restClient = builder.baseUrl(url).build();
        this.username = username;
        this.password = password;
        this.clock = clock;
    }

    /** Retourne un jeton valide, en en demandant un nouveau si nécessaire. */
    public synchronized String getToken() {
        if (token == null || !clock.instant().isBefore(refreshAt)) {
            fetch();
        }
        return token;
    }

    /** Oublie le jeton courant (par exemple après un refus par le service appelé). */
    public synchronized void invalidate() {
        token = null;
    }

    private void fetch() {
        TokenResponse response;
        try {
            response = restClient.post()
                    .uri("/token")
                    .body(Map.of("username", username, "password", password))
                    .retrieve()
                    .body(TokenResponse.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Impossible d'obtenir un jeton auprès d'arc-auth", e);
        }
        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("Réponse d'arc-auth sans jeton");
        }
        token = response.accessToken();
        refreshAt = clock.instant().plusSeconds(response.expiresIn()).minus(SAFETY_MARGIN);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn) {
    }
}
