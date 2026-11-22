// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import secop.ppm1.domain.model.Profile;

class ArcPublisherJobAdapterTest {

    private final UUID jobId = UUID.randomUUID();

    private MockRestServiceServer server;
    private ArcAuthTokenProvider tokens;
    private ArcPublisherJobAdapter adapter;
    private Profile profile;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        tokens = mock(ArcAuthTokenProvider.class);
        adapter = new ArcPublisherJobAdapter(builder, "http://arc-publisher", "ad://DC01", "product-manager", tokens);
        profile = new Profile();
        profile.setName("GRP-FIN");
        profile.setDescription("Finance");
    }

    private String idJson() {
        return "\"" + jobId + "\"";
    }

    @Test
    void sendsTheJobWithTheServiceAccountToken() {
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/jobs"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t1"))
                .andExpect(jsonPath("$.action").value("CREATE"))
                .andExpect(jsonPath("$.entitlementId").value("GRP-FIN"))
                .andRespond(withSuccess(idJson(), MediaType.APPLICATION_JSON));

        assertEquals(jobId, adapter.publishCreateEntitlement("Compta", profile));

        server.verify();
    }

    @Test
    void renewsTheTokenAndRetriesOnceWhenTheTokenIsRefused() {
        when(tokens.getToken()).thenReturn("old", "new");
        server.expect(requestTo("http://arc-publisher/jobs"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer old"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo("http://arc-publisher/jobs"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer new"))
                .andRespond(withSuccess(idJson(), MediaType.APPLICATION_JSON));

        assertEquals(jobId, adapter.publishUpdateEntitlement("Compta", profile));

        verify(tokens).invalidate();
        server.verify();
    }

    @Test
    void givesUpAfterASecondRefusal() {
        when(tokens.getToken()).thenReturn("t");
        server.expect(requestTo("http://arc-publisher/jobs")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo("http://arc-publisher/jobs")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.Unauthorized.class,
                () -> adapter.publishDeleteEntitlement("Compta", profile));

        verify(tokens, times(1)).invalidate();
    }

    @Test
    void doesNotRetryOnForbidden() {
        when(tokens.getToken()).thenReturn("t");
        server.expect(requestTo("http://arc-publisher/jobs")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThrows(HttpClientErrorException.Forbidden.class,
                () -> adapter.publishCreateEntitlement("Compta", profile));

        server.verify();
    }
}
