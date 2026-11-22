// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.ProvisioningOrder;

class KafkaOrderPublisherTest {

    private static final OffsetDateTime AT = OffsetDateTime.parse("2026-07-01T00:00:00Z");

    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    private KafkaTemplate<String, String> kafka;
    private KafkaOrderPublisher publisher;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafka = mock(KafkaTemplate.class);
        publisher = new KafkaOrderPublisher(kafka, mapper, "arc.orders", "ad://DC01", "product-manager");
    }

    @SuppressWarnings("unchecked")
    private void sendSucceeds(UUID id) {
        when(kafka.send(eq("arc.orders"), eq(id.toString()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
    }

    private ProvisioningOrder profileOrder(ProvisioningOrder.Action action) {
        Profile profile = new Profile();
        profile.setName("GRP-FIN");
        profile.setDescription("Finance");
        profile.setScope("TIER1");
        return ProvisioningOrder.forProfile(action, "Compta", profile, AT, "profile:Compta:GRP-FIN");
    }

    @Test
    void submitsAFlatProfileOrderMessageKeyedByTheOrderId() throws Exception {
        ProvisioningOrder order = profileOrder(ProvisioningOrder.Action.CREATE);
        sendSucceeds(order.id());

        publisher.submit(order);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(eq("arc.orders"), eq(order.id().toString()), json.capture());
        JsonNode message = mapper.readTree(json.getValue());
        assertEquals("SUBMIT", message.get("operation").asText());
        assertEquals(order.id().toString(), message.get("id").asText());
        assertEquals("CREATE", message.get("action").asText());
        assertEquals("ENTITLEMENT", message.get("targetType").asText());
        assertEquals("Compta", message.get("identityId").asText());
        assertEquals("GRP-FIN", message.get("entitlementId").asText());
        assertEquals("Finance", message.get("entitlementName").asText());
        assertEquals("TIER1", message.get("scope").asText());
        assertEquals("ad://DC01", message.get("targetSystem").asText());
        assertEquals("product-manager", message.get("source").asText());
        assertEquals("2026-07-01T00:00:00Z", message.get("executeAt").asText());
    }

    @Test
    void submitsAnAccountOrderAndTheMessageCanBeReadBack() throws Exception {
        Account account = new Account();
        account.setIdentifier("svc-compta-batch");
        account.setType("service-account");
        account.setProduct("compta");
        account.setMetadata(Map.of("owner", "alice"));
        ProvisioningOrder order = ProvisioningOrder.forAccount(ProvisioningOrder.Action.DELETE, account, AT,
                "account:svc-compta-batch:end");
        sendSucceeds(order.id());

        publisher.submit(order);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(eq("arc.orders"), eq(order.id().toString()), json.capture());
        OrderRequestDto dto = mapper.readValue(json.getValue(), OrderRequestDto.class);
        assertEquals("SUBMIT", dto.getOperation());
        assertEquals(order.id(), dto.getId());
        assertEquals("DELETE", dto.getAction());
        assertEquals("IDENTITY", dto.getTargetType());
        assertEquals("svc-compta-batch", dto.getIdentityId());
        assertEquals("svc-compta-batch", dto.getIdentityName());
        assertEquals("service-account", dto.getAccountType());
        assertEquals("ad://DC01", dto.getTargetSystem());
        assertEquals("product-manager", dto.getSource());
        assertEquals("compta", dto.getAttributes().get("product"));
        assertEquals("alice", dto.getAttributes().get("metadata.owner"));
        assertEquals(AT.toInstant(), dto.getExecuteAt().toInstant());
        assertEquals("account:svc-compta-batch:end", dto.getCorrelationId());
    }

    @Test
    void readsBackAProfileOrderMessage() throws Exception {
        ProvisioningOrder order = profileOrder(ProvisioningOrder.Action.DELETE);
        sendSucceeds(order.id());

        publisher.submit(order);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(eq("arc.orders"), eq(order.id().toString()), json.capture());
        OrderRequestDto dto = mapper.readValue(json.getValue(), OrderRequestDto.class);
        assertEquals("ENTITLEMENT", dto.getTargetType());
        assertEquals("Compta", dto.getIdentityName());
        assertEquals("GRP-FIN", dto.getEntitlementId());
        assertEquals("Finance", dto.getEntitlementName());
        assertEquals("TIER1", dto.getScope());
    }

    @Test
    void cancelsAnOrderWithAMinimalMessage() throws Exception {
        UUID id = UUID.randomUUID();
        sendSucceeds(id);

        publisher.cancel(id);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(eq("arc.orders"), eq(id.toString()), json.capture());
        JsonNode message = mapper.readTree(json.getValue());
        assertEquals("CANCEL", message.get("operation").asText());
        assertEquals(id.toString(), message.get("id").asText());
        assertEquals(2, message.size());
    }

    @Test
    void reportsABrokerFailure() {
        UUID id = UUID.randomUUID();
        when(kafka.send(eq("arc.orders"), eq(id.toString()), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        assertThrows(IllegalStateException.class, () -> publisher.cancel(id));
    }

    @Test
    void keepsTheInterruptionFlag() {
        UUID id = UUID.randomUUID();
        CompletableFuture<SendResult<String, String>> never = new CompletableFuture<>();
        when(kafka.send(eq("arc.orders"), eq(id.toString()), anyString())).thenReturn(never);

        Thread.currentThread().interrupt();
        try {
            assertThrows(IllegalStateException.class, () -> publisher.cancel(id));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
