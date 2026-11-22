// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import secop.ppm1.domain.model.ProvisioningOrder;
import secop.ppm1.domain.port.OrderPublisher;

/** Canal API : les ordres sont déposés par appel REST à arc-publisher ({@code ARC_ORDERS_CHANNEL=api}). */
@Component
@ConditionalOnProperty(name = "arc.orders.channel", havingValue = "api")
public class ArcPublisherOrderAdapter implements OrderPublisher {

    private final RestClient restClient;
    private final ArcAuthTokenProvider tokenProvider;
    private final String targetSystem;
    private final String source;

    @Autowired
    public ArcPublisherOrderAdapter(
            @Value("${arc.publisher.url}") String arcPublisherUrl,
            @Value("${arc.publisher.target-system}") String targetSystem,
            @Value("${arc.publisher.source}") String source,
            ArcAuthTokenProvider tokenProvider) {
        this(RestClient.builder(), arcPublisherUrl, targetSystem, source, tokenProvider);
    }

    ArcPublisherOrderAdapter(RestClient.Builder builder, String arcPublisherUrl, String targetSystem,
            String source, ArcAuthTokenProvider tokenProvider) {
        this.restClient = builder.baseUrl(arcPublisherUrl).build();
        this.tokenProvider = tokenProvider;
        this.targetSystem = targetSystem;
        this.source = source;
    }

    @Override
    public void submit(ProvisioningOrder order) {
        OrderRequestDto request = OrderRequestDto.from(order, targetSystem, source);
        withRetry(() -> restClient.post()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(tokenProvider.getToken()))
                .body(request)
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public void cancel(UUID orderId) {
        try {
            withRetry(() -> restClient.delete()
                    .uri("/orders/{id}", orderId)
                    .headers(headers -> headers.setBearerAuth(tokenProvider.getToken()))
                    .retrieve()
                    .toBodilessEntity());
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.Conflict e) {
            // Ordre inconnu, ou déjà exécuté / en cours : il n'y a plus rien à annuler.
        }
    }

    private void withRetry(Runnable call) {
        try {
            call.run();
        } catch (HttpClientErrorException.Unauthorized e) {
            // Jeton refusé (clés d'arc-auth renouvelées, par exemple) : un nouveau jeton, une seule nouvelle tentative.
            tokenProvider.invalidate();
            call.run();
        }
    }
}
