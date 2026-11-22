// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.consume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import secop.ppm1.domain.service.AccountManager;

/**
 * Découvre les comptes non utilisateurs dans les événements d'identité consolidés (arc.sync.identity).
 * Désactivé par défaut : {@code arc.discovery.enabled=true} pour l'activer.
 */
@Component
@ConditionalOnProperty(name = "arc.discovery.enabled", havingValue = "true")
public class AccountDiscoveryListener {

    private static final Logger log = LoggerFactory.getLogger(AccountDiscoveryListener.class);

    private final AccountManager accountManager;
    private final ObjectMapper objectMapper;

    public AccountDiscoveryListener(AccountManager accountManager, ObjectMapper objectMapper) {
        this.accountManager = accountManager;
        this.objectMapper = objectMapper;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IdentityEvent(String eventType, String name, String displayName) {
        String identifier() {
            return displayName != null && !displayName.isBlank() ? displayName : name;
        }
    }

    @KafkaListener(topics = "${arc.discovery.topic:arc.sync.identity}")
    public void onIdentityEvent(String payload) {
        try {
            IdentityEvent event = objectMapper.readValue(payload, IdentityEvent.class);
            accountManager.discover(event.identifier(), event.eventType());
        } catch (JsonProcessingException e) {
            // Un message illisible ne doit pas bloquer la consommation du topic.
            log.warn("Ignored unreadable identity event: {}", e.getOriginalMessage());
        }
    }
}
