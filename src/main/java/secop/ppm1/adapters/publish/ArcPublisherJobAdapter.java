// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import secop.ppm1.domain.model.Account;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.port.JobPublisher;

@Component
public class ArcPublisherJobAdapter implements JobPublisher {

    private static final String TARGET_TYPE = "ENTITLEMENT";
    private static final String ACCOUNT_TARGET_TYPE = "IDENTITY";

    private final RestClient restClient;
    private final ArcAuthTokenProvider tokenProvider;
    private final String targetSystem;
    private final String source;

    @Autowired
    public ArcPublisherJobAdapter(
            @Value("${arc.publisher.url}") String arcPublisherUrl,
            @Value("${arc.publisher.target-system}") String targetSystem,
            @Value("${arc.publisher.source}") String source,
            ArcAuthTokenProvider tokenProvider) {
        this(RestClient.builder(), arcPublisherUrl, targetSystem, source, tokenProvider);
    }

    ArcPublisherJobAdapter(RestClient.Builder builder, String arcPublisherUrl, String targetSystem,
            String source, ArcAuthTokenProvider tokenProvider) {
        this.restClient = builder.baseUrl(arcPublisherUrl).build();
        this.tokenProvider = tokenProvider;
        this.targetSystem = targetSystem;
        this.source = source;
    }

    @Override
    public UUID publishCreateEntitlement(String productName, Profile profile) {
        return sendJob("CREATE", productName, profile);
    }

    @Override
    public UUID publishUpdateEntitlement(String productName, Profile profile) {
        return sendJob("MODIFY", productName, profile);
    }

    @Override
    public UUID publishDeleteEntitlement(String productName, Profile profile) {
        return sendJob("DELETE", productName, profile);
    }

    @Override
    public UUID publishCreateAccount(Account account) {
        return sendAccountJob("CREATE", account);
    }

    @Override
    public UUID publishDeleteAccount(Account account) {
        return sendAccountJob("DELETE", account);
    }

    private UUID sendAccountJob(String action, Account account) {
        CreateJobRequestDto request = new CreateJobRequestDto();
        request.setAction(action);
        request.setTargetType(ACCOUNT_TARGET_TYPE);
        request.setIdentityId(account.getIdentifier());
        request.setIdentityName(account.getIdentifier());
        request.setTargetSystem(targetSystem);
        request.setSource(source);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("accountType", account.getType());
        if (account.getProduct() != null) {
            attributes.put("product", account.getProduct());
        }
        if (account.getMetadata() != null) {
            account.getMetadata().forEach((key, value) -> attributes.put("metadata." + key, String.valueOf(value)));
        }
        request.setAttributes(attributes);
        request.setExecuteAt(OffsetDateTime.now());
        request.setCorrelationId(UUID.randomUUID().toString());
        return sendWithRetry(request);
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
        return sendWithRetry(request);
    }

    private UUID sendWithRetry(CreateJobRequestDto request) {
        try {
            return post(request);
        } catch (HttpClientErrorException.Unauthorized e) {
            // Jeton refusé (clés d'arc-auth renouvelées, par exemple) : un nouveau jeton, une seule nouvelle tentative.
            tokenProvider.invalidate();
            return post(request);
        }
    }

    private UUID post(CreateJobRequestDto request) {
        return restClient.post()
                .uri("/jobs")
                .headers(headers -> headers.setBearerAuth(tokenProvider.getToken()))
                .body(request)
                .retrieve()
                .body(UUID.class);
    }
}
