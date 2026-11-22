// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ArcAuthTokenProviderTest {

    private static final String TOKEN_JSON =
            "{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":900}";

    private MockRestServiceServer server;
    private MutableClock clock;
    private ArcAuthTokenProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        clock = new MutableClock(Instant.parse("2030-01-01T10:00:00Z"));
        provider = new ArcAuthTokenProvider(builder, "http://arc-auth", "svc", "secret", clock);
    }

    private void expectTokenRequest(String token) {
        server.expect(requestTo("http://arc-auth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.username").value("svc"))
                .andExpect(jsonPath("$.password").value("secret"))
                .andRespond(withSuccess(TOKEN_JSON.formatted(token), MediaType.APPLICATION_JSON));
    }

    @Test
    void reusesTheTokenWhileItIsValid() {
        expectTokenRequest("t1");

        assertEquals("t1", provider.getToken());
        clock.advanceSeconds(800);
        assertEquals("t1", provider.getToken());

        server.verify();
    }

    @Test
    void renewsTheTokenShortlyBeforeExpiry() {
        expectTokenRequest("t1");
        expectTokenRequest("t2");

        assertEquals("t1", provider.getToken());
        clock.advanceSeconds(900 - 29);
        assertEquals("t2", provider.getToken());

        server.verify();
    }

    @Test
    void fetchesANewTokenAfterInvalidation() {
        expectTokenRequest("t1");
        expectTokenRequest("t2");

        assertEquals("t1", provider.getToken());
        provider.invalidate();
        assertEquals("t2", provider.getToken());

        server.verify();
    }

    @Test
    void failsClearlyWhenArcAuthRefusesTheServiceAccount() {
        server.expect(requestTo("http://arc-auth/token"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        IllegalStateException e = assertThrows(IllegalStateException.class, provider::getToken);

        assertEquals("Impossible d'obtenir un jeton auprès d'arc-auth", e.getMessage());
    }

    @Test
    void failsWhenTheResponseHasNoToken() {
        server.expect(requestTo("http://arc-auth/token"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"expires_in\":900}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class, provider::getToken);
    }

    /** Horloge dont on fait avancer le temps à la main. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advanceSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
