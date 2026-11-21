// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.port.JobPublisher;

@Component
public class ArcPublisherJobAdapter implements JobPublisher {

    private static final String TARGET_TYPE = "ENTITLEMENT";

    private final RestClient restClient;
    private final String targetSystem;
    private final String source;

    public ArcPublisherJobAdapter(
            @Value("${arc.publisher.url}") String arcPublisherUrl,
            @Value("${arc.publisher.target-system}") String targetSystem,
            @Value("${arc.publisher.source}") String source) {
        this.restClient = RestClient.builder()
                .baseUrl(arcPublisherUrl)
                .build();
        this.targetSystem = targetSystem;
        this.source = source;
    }

    @Override
    public UUID publishCreateEntitlement(String productName, Profile profile) {
        return sendJob("CREATE", productName, profile);
    }

    @Override
    public UUID publishUpdateEntitlement(String productName, Profile profile) {
        return sendJob("UPDATE", productName, profile);
    }

    @Override
    public UUID publishDeleteEntitlement(String productName, Profile profile) {
        return sendJob("DELETE", productName, profile);
    }

    private UUID sendJob(String action, String productName, Profile profile) {
        CreateJobRequestDto request = new CreateJobRequestDto();
        request.setAction(action);
        request.setTargetType(TARGET_TYPE);
        request.setIdentityId(productName);
        request.setIdentityName(productName);
        request.setTargetSystem(targetSystem);
        request.setEntitlementId(profile.getName());
        request.setEntitlementName(profile.getDescription() != null ? profile.getDescription() : profile.getName());
        request.setScope(profile.getScope());
        request.setSource(source);
        request.setExecuteAt(OffsetDateTime.now());
        request.setCorrelationId(UUID.randomUUID().toString());

        return restClient.post()
                .uri("/jobs")
                .body(request)
                .retrieve()
                .body(UUID.class);
    }
}
