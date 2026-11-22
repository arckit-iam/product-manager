// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.ProvisioningOrder;

class ArcPublisherOrderAdapterTest {

    private static final OffsetDateTime AT = OffsetDateTime.parse("2026-07-01T00:00:00Z");

    private MockRestServiceServer server;
    private ArcAuthTokenProvider tokens;
    private ArcPublisherOrderAdapter adapter;
    private Profile profile;
    private ProvisioningOrder profileOrder;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        tokens = mock(ArcAuthTokenProvider.class);
        adapter = new ArcPublisherOrderAdapter(builder, "http://arc-publisher", "ad://DC01", "product-manager",
                tokens);
        profile = new Profile();
        profile.setName("GRP-FIN");
        profile.setDescription("Finance");
        profile.setScope("TIER1");
        profileOrder = ProvisioningOrder.forProfile(ProvisioningOrder.Action.CREATE, "Compta", profile, AT,
                "profile:Compta:GRP-FIN");
    }

    private String idJson() {
        return "\"" + profileOrder.id() + "\"";
    }

    @Test
    void submitsAProfileOrderWithTheServiceAccountToken() {
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t1"))
                .andExpect(jsonPath("$.id").value(profileOrder.id().toString()))
                .andExpect(jsonPath("$.action").value("CREATE"))
                .andExpect(jsonPath("$.targetType").value("ENTITLEMENT"))
                .andExpect(jsonPath("$.identityId").value("Compta"))
                .andExpect(jsonPath("$.entitlementId").value("GRP-FIN"))
                .andExpect(jsonPath("$.entitlementName").value("Finance"))
                .andExpect(jsonPath("$.scope").value("TIER1"))
                .andExpect(jsonPath("$.source").value("product-manager"))
                .andExpect(jsonPath("$.correlationId").value("profile:Compta:GRP-FIN"))
                .andExpect(jsonPath("$.operation").doesNotExist())
                .andExpect(jsonPath("$.attributes").doesNotExist())
                .andRespond(withSuccess(idJson(), MediaType.APPLICATION_JSON));

        adapter.submit(profileOrder);

        server.verify();
    }

    @Test
    void aProfileWithoutDescriptionIsNamedAfterItsId() {
        profile.setDescription(null);
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/orders"))
                .andExpect(jsonPath("$.entitlementName").value("GRP-FIN"))
                .andRespond(withSuccess(idJson(), MediaType.APPLICATION_JSON));

        adapter.submit(profileOrder);

        server.verify();
    }

    @Test
    void submitsAnAccountOrderWithItsProductAndMetadata() {
        Account account = new Account();
        account.setIdentifier("svc-compta-batch");
        account.setType("service-account");
        account.setProduct("compta");
        account.setMetadata(Map.of("owner", "alice"));
        ProvisioningOrder order = ProvisioningOrder.forAccount(ProvisioningOrder.Action.DELETE, account, AT,
                "account:svc-compta-batch:end");
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/orders"))
                .andExpect(jsonPath("$.action").value("DELETE"))
                .andExpect(jsonPath("$.targetType").value("IDENTITY"))
                .andExpect(jsonPath("$.identityId").value("svc-compta-batch"))
                .andExpect(jsonPath("$.accountType").value("service-account"))
                .andExpect(jsonPath("$.attributes.product").value("compta"))
                .andExpect(jsonPath("$.attributes['metadata.owner']").value("alice"))
                .andRespond(withSuccess("\"" + order.id() + "\"", MediaType.APPLICATION_JSON));

        adapter.submit(order);

        server.verify();
    }

    @Test
    void renewsTheTokenAndRetriesOnceWhenTheTokenIsRefused() {
        when(tokens.getToken()).thenReturn("old", "new");
        server.expect(requestTo("http://arc-publisher/orders"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer old"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo("http://arc-publisher/orders"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer new"))
                .andRespond(withSuccess(idJson(), MediaType.APPLICATION_JSON));

        adapter.submit(profileOrder);

        verify(tokens).invalidate();
        server.verify();
    }

    @Test
    void givesUpAfterASecondRefusal() {
        when(tokens.getToken()).thenReturn("t");
        server.expect(requestTo("http://arc-publisher/orders")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo("http://arc-publisher/orders")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.Unauthorized.class, () -> adapter.submit(profileOrder));

        verify(tokens, times(1)).invalidate();
    }

    @Test
    void cancelsAnOrder() {
        UUID id = profileOrder.id();
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/orders/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer t1"))
                .andRespond(withNoContent());

        adapter.cancel(id);

        server.verify();
    }

    @Test
    void cancellingAnUnknownOrAlreadyHandledOrderIsHarmless() {
        UUID id = profileOrder.id();
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/orders/" + id)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo("http://arc-publisher/orders/" + id)).andRespond(withStatus(HttpStatus.CONFLICT));

        assertDoesNotThrow(() -> adapter.cancel(id));
        assertDoesNotThrow(() -> adapter.cancel(id));

        server.verify();
    }

    @Test
    void aFailingPublisherIsReportedOnCancel() {
        UUID id = profileOrder.id();
        when(tokens.getToken()).thenReturn("t1");
        server.expect(requestTo("http://arc-publisher/orders/" + id)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(HttpServerErrorException.class, () -> adapter.cancel(id));
    }
}
